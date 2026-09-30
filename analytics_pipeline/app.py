import os
import json
import time
from datetime import datetime
import urllib.request
from flask import Flask, jsonify, render_template_string, request
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

app = Flask(__name__)

FIRESTORE_URL = "https://firestore.googleapis.com/v1/projects/uniandesfood/databases/(default)/documents/telemetry_events"

RESTAURANT_NAMES = {
    "el_toro_rgd": "El Toro (RGD)",
    "one_burrito_ml": "One Burrito (ML)",
    "one_burrito_rgd": "One Burrito (RGD)",
    "burger_play_rgd": "Burger Play (RGD)",
    "burger_play_sd": "Burger Play (SD)",
    "la_cabra_sanduchera_rgd": "La Cabra Sanduchera",
    "la_liebre_franco": "La Liebre (Franco)"
}

def parse_firestore_value(val):
    if not isinstance(val, dict):
        return val
    if "stringValue" in val:
        return val["stringValue"]
    if "integerValue" in val:
        return int(val["integerValue"])
    if "doubleValue" in val:
        return float(val["doubleValue"])
    if "booleanValue" in val:
        return bool(val["booleanValue"])
    if "arrayValue" in val:
        return [parse_firestore_value(v) for v in val["arrayValue"].get("values", [])]
    if "mapValue" in val:
        return {k: parse_firestore_value(v) for k, v in val["mapValue"].get("fields", {}).items()}
    return None

def fetch_telemetry_events():
    try:
        req = urllib.request.Request(FIRESTORE_URL, headers={"Accept": "application/json"})
        with urllib.request.urlopen(req, timeout=5) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            docs = data.get("documents", [])
            events = []
            for doc in docs:
                fields = doc.get("fields", {})
                event = {k: parse_firestore_value(v) for k, v in fields.items()}
                event["doc_id"] = doc.get("name", "").split("/")[-1]
                events.append(event)
            return sorted(events, key=lambda x: x.get("timestamp", 0), reverse=True)
    except Exception as e:
        print(f"Error fetching Firestore telemetry: {e}")
        return []

def calculate_metrics(events):
    # 1. BQ6 - Samuel: Filter Session Duration, Buildings, Dietary, Budgets
    filter_events = [e for e in events if e.get("eventName") == "samuel_bq_filter_session"]
    durations = [float(e.get("params", {}).get("duration_sec", 0)) for e in filter_events if "duration_sec" in e.get("params", {})]
    
    avg_duration = round(sum(durations) / len(durations), 1) if durations else "N/A"

    buildings = {"ML": 0, "RGD": 0, "Franco": 0, "SD": 0, "C": 0, "W": 0}
    dietary_counts = {"Vegan": 0, "Gluten-Free": 0}
    budget_counts = {"CHEAP": 0, "MEDIUM": 0, "HIGH": 0}
    duration_buckets = {"< 5s": 0, "5-10s": 0, "10-20s": 0, "> 20s": 0}

    for e in filter_events:
        params = e.get("params", {})
        bldg = params.get("building", "ML")
        buildings[bldg] = buildings.get(bldg, 0) + 1

        if params.get("is_vegan") is True: dietary_counts["Vegan"] += 1
        if params.get("is_gluten_free") is True: dietary_counts["Gluten-Free"] += 1

        bg = params.get("budget_range", "MEDIUM")
        budget_counts[bg] = budget_counts.get(bg, 0) + 1

        d = float(params.get("duration_sec", 0))
        if d < 5: duration_buckets["< 5s"] += 1
        elif d <= 10: duration_buckets["5-10s"] += 1
        elif d <= 20: duration_buckets["10-20s"] += 1
        else: duration_buckets["> 20s"] += 1

    # 2. BQ4 - Karin: Menu Photo Inspection
    inspection_events = [e for e in events if e.get("eventName") == "karin_bq_menu_inspection"]
    inspected_count = sum(1 for e in inspection_events if e.get("params", {}).get("checked_photos", False))
    total_inspections = len(inspection_events)
    inspection_rate = round((inspected_count / total_inspections) * 100, 1) if total_inspections > 0 else "N/A"

    inspected_restaurants = {}
    for e in inspection_events:
        params = e.get("params", {})
        rid = params.get("restaurant_id", "general")
        friendly_name = RESTAURANT_NAMES.get(rid, rid)
        inspected_restaurants[friendly_name] = inspected_restaurants.get(friendly_name, 0) + 1

    # 3. QR Reviews Telemetry
    review_events = [e for e in events if e.get("eventName") == "qr_review_submission"]
    ratings = [int(e.get("params", {}).get("rating", 0)) for e in review_events if e.get("params", {}).get("is_completed", False) and int(e.get("params", {}).get("rating", 0)) > 0]
    avg_rating = round(sum(ratings) / len(ratings), 1) if ratings else "N/A"
    completed_reviews = sum(1 for e in review_events if e.get("params", {}).get("is_completed", False))
    cancelled_reviews = sum(1 for e in review_events if not e.get("params", {}).get("is_completed", True))

    rating_distribution = {1: 0, 2: 0, 3: 0, 4: 0, 5: 0}
    for r in ratings:
        if r in rating_distribution:
            rating_distribution[r] += 1

    return {
        "summary": {
            "total_events": len(events),
            "filter_sessions": len(filter_events),
            "menu_inspections": len(inspection_events),
            "qr_reviews": len(review_events),
            "avg_filter_duration_sec": avg_duration,
            "menu_inspection_rate_pct": inspection_rate,
            "avg_qr_rating": avg_rating
        },
        "bq6_samuel": {
            "avg_duration": avg_duration,
            "duration_buckets": duration_buckets,
            "buildings": buildings,
            "dietary": dietary_counts,
            "budgets": budget_counts
        },
        "bq4_karin": {
            "inspection_rate": inspection_rate,
            "checked_count": inspected_count,
            "not_checked_count": total_inspections - inspected_count,
            "inspected_restaurants": inspected_restaurants
        },
        "qr_reviews": {
            "avg_rating": avg_rating,
            "completed": completed_reviews,
            "cancelled": cancelled_reviews,
            "ratings_breakdown": rating_distribution
        },
        "raw_events": events[:30]
    }

@app.route("/api/stats")
def api_stats():
    events = fetch_telemetry_events()
    return jsonify(calculate_metrics(events))

@app.route("/api/telemetry", methods=["POST"])
def api_receive_telemetry():
    """Endpoint for direct HTTP event ingestion"""
    try:
        data = request.get_json(force=True)
        return jsonify({"status": "received", "event": data}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 400

@app.route("/")
def dashboard():
    html = """
    <!DOCTYPE html>
    <html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Uniandes Food - Analytics Pipeline & BQ Dashboard</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
        <style>
            @keyframes pulse-live {
                0%, 100% { opacity: 1; transform: scale(1); }
                50% { opacity: 0.5; transform: scale(1.1); }
            }
            .live-dot {
                animation: pulse-live 1.5s infinite;
            }
        </style>
    </head>
    <body class="bg-slate-950 text-slate-100 font-sans min-h-screen">
        <!-- Top Navbar -->
        <header class="bg-slate-900/90 backdrop-blur border-b border-slate-800 sticky top-0 z-50 px-6 py-4 flex items-center justify-between">
            <div class="flex items-center space-x-4">
                <div class="w-10 h-10 rounded-xl bg-amber-500/20 border border-amber-500/40 flex items-center justify-center text-amber-400 font-bold text-xl">
                    UF
                </div>
                <div>
                    <h1 class="text-xl font-bold text-white tracking-wide">Uniandes Food - Analytics Pipeline</h1>
                    <p class="text-xs text-slate-400">ISIS-3510 Desarrollo de Aplicaciones Móviles • Sprint 2 • Grupo GN-44</p>
                </div>
            </div>
            <div class="flex items-center space-x-3">
                <div class="flex items-center space-x-2 bg-emerald-950/80 border border-emerald-700/50 px-3 py-1.5 rounded-full text-xs font-semibold text-emerald-400">
                    <span class="w-2.5 h-2.5 rounded-full bg-emerald-400 live-dot"></span>
                    <span>FIRESTORE LIVE STREAM</span>
                </div>
                <button onclick="fetchData()" class="bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs px-3 py-1.5 rounded-lg border border-slate-700 transition">
                    <i class="fa-solid fa-rotate-right mr-1"></i> Actualizar
                </button>
            </div>
        </header>

        <main class="max-w-7xl mx-auto px-6 py-8 space-y-8">
            <!-- Summary KPI Cards -->
            <div class="grid grid-cols-1 md:grid-cols-4 gap-5">
                <div class="bg-slate-900 border border-slate-800 p-5 rounded-2xl shadow-lg relative overflow-hidden">
                    <div class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Total Eventos Telemetría</div>
                    <div class="text-3xl font-extrabold text-white mt-2" id="kpi-total">0</div>
                    <div class="text-xs text-slate-500 mt-2 flex items-center">
                        <i class="fa-solid fa-cloud text-amber-400 mr-1.5"></i> Sincronizados en Firestore
                    </div>
                </div>

                <div class="bg-slate-900 border border-slate-800 p-5 rounded-2xl shadow-lg relative overflow-hidden">
                    <div class="text-xs font-semibold text-amber-400 uppercase tracking-wider">BQ6 - Tiempo en Filtros</div>
                    <div class="text-3xl font-extrabold text-amber-400 mt-2"><span id="kpi-duration">N/A</span> <span class="text-lg font-normal text-slate-400" id="kpi-duration-unit"></span></div>
                    <div class="text-xs text-slate-500 mt-2 flex items-center">
                        <i class="fa-solid fa-stopwatch text-amber-400 mr-1.5"></i> Autor: Samuel (Tipo 2)
                    </div>
                </div>

                <div class="bg-slate-900 border border-slate-800 p-5 rounded-2xl shadow-lg relative overflow-hidden">
                    <div class="text-xs font-semibold text-emerald-400 uppercase tracking-wider">BQ4 - Tasa Inspección Fotos</div>
                    <div class="text-3xl font-extrabold text-emerald-400 mt-2"><span id="kpi-inspection">N/A</span> <span class="text-lg font-normal text-slate-400" id="kpi-inspection-unit"></span></div>
                    <div class="text-xs text-slate-500 mt-2 flex items-center">
                        <i class="fa-solid fa-images text-emerald-400 mr-1.5"></i> Autora: Karin (Tipo 2)
                    </div>
                </div>

                <div class="bg-slate-900 border border-slate-800 p-5 rounded-2xl shadow-lg relative overflow-hidden">
                    <div class="text-xs font-semibold text-indigo-400 uppercase tracking-wider">Promedio Calificación QR</div>
                    <div class="text-3xl font-extrabold text-indigo-400 mt-2"><span id="kpi-rating">N/A</span> <span class="text-lg text-amber-400" id="kpi-rating-unit"></span></div>
                    <div class="text-xs text-slate-500 mt-2 flex items-center">
                        <i class="fa-solid fa-qrcode text-indigo-400 mr-1.5"></i> Reseñas post-escaneo
                    </div>
                </div>
            </div>

            <!-- BQ Section 1: Samuel BQ6 -->
            <section class="bg-slate-900 border border-slate-800 p-6 rounded-3xl space-y-6">
                <div class="flex items-center justify-between border-b border-slate-800 pb-4">
                    <div>
                        <div class="inline-flex items-center space-x-2 bg-amber-500/10 text-amber-400 text-xs px-2.5 py-1 rounded-md font-semibold mb-1">
                            <span>BUSINESS QUESTION 6 • SAMUEL</span>
                        </div>
                        <h2 class="text-xl font-bold text-white">¿Cuánto tiempo en promedio gasta un estudiante configurando filtros de precio, distancia y dietas antes de elegir restaurante?</h2>
                    </div>
                </div>

                <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
                    <!-- Chart 1: Duration Distribution -->
                    <div class="bg-slate-950 p-5 rounded-2xl border border-slate-800/80">
                        <h3 class="text-sm font-semibold text-slate-300 mb-3">Distribución de Tiempo en Filtros (segundos)</h3>
                        <div class="h-56">
                            <canvas id="chart-durations"></canvas>
                        </div>
                    </div>

                    <!-- Chart 2: Buildings Used -->
                    <div class="bg-slate-950 p-5 rounded-2xl border border-slate-800/80">
                        <h3 class="text-sm font-semibold text-slate-300 mb-3">Edificios Uniandes de Referencia</h3>
                        <div class="h-56">
                            <canvas id="chart-buildings"></canvas>
                        </div>
                    </div>

                    <!-- Chart 3: Dietary & Budget -->
                    <div class="bg-slate-950 p-5 rounded-2xl border border-slate-800/80">
                        <h3 class="text-sm font-semibold text-slate-300 mb-3">Preferencias Dietarias Filtradas</h3>
                        <div class="h-56">
                            <canvas id="chart-dietary"></canvas>
                        </div>
                    </div>
                </div>
            </section>

            <!-- BQ Section 2: Karin BQ4 & QR Reviews -->
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <!-- Karin BQ4 -->
                <section class="bg-slate-900 border border-slate-800 p-6 rounded-3xl space-y-6">
                    <div>
                        <div class="inline-flex items-center space-x-2 bg-emerald-500/10 text-emerald-400 text-xs px-2.5 py-1 rounded-md font-semibold mb-1">
                            <span>BUSINESS QUESTION 4 • KARIN</span>
                        </div>
                        <h2 class="text-lg font-bold text-white">¿Qué porcentaje de estudiantes revisa fotos del menú antes de decidir?</h2>
                    </div>

                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        <div class="bg-slate-950 p-4 rounded-2xl border border-slate-800/80">
                            <h3 class="text-xs font-semibold text-slate-400 mb-2">Revisión de Fotos de Platos</h3>
                            <div class="h-44">
                                <canvas id="chart-photos"></canvas>
                            </div>
                        </div>
                        <div class="bg-slate-950 p-4 rounded-2xl border border-slate-800/80">
                            <h3 class="text-xs font-semibold text-slate-400 mb-2">Locales Más Inspeccionados</h3>
                            <div class="h-44">
                                <canvas id="chart-inspected-locales"></canvas>
                            </div>
                        </div>
                    </div>
                </section>

                <!-- QR Review Analytics -->
                <section class="bg-slate-900 border border-slate-800 p-6 rounded-3xl space-y-6">
                    <div>
                        <div class="inline-flex items-center space-x-2 bg-indigo-500/10 text-indigo-400 text-xs px-2.5 py-1 rounded-md font-semibold mb-1">
                            <span>SENSOR & QR REVIEWS • FLUJO</span>
                        </div>
                        <h2 class="text-lg font-bold text-white">Satisfacción y Completitud de Reseñas por QR</h2>
                    </div>

                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        <div class="bg-slate-950 p-4 rounded-2xl border border-slate-800/80">
                            <h3 class="text-xs font-semibold text-slate-400 mb-2">Distribución de Estrellas</h3>
                            <div class="h-44">
                                <canvas id="chart-rating-dist"></canvas>
                            </div>
                        </div>
                        <div class="bg-slate-950 p-4 rounded-2xl border border-slate-800/80">
                            <h3 class="text-xs font-semibold text-slate-400 mb-2">Completitud de Reseñas</h3>
                            <div class="h-44">
                                <canvas id="chart-completion"></canvas>
                            </div>
                        </div>
                    </div>
                </section>
            </div>

            <!-- Live Event Stream Table -->
            <section class="bg-slate-900 border border-slate-800 p-6 rounded-3xl space-y-4">
                <div class="flex items-center justify-between">
                    <div>
                        <h2 class="text-lg font-bold text-white">Stream de Eventos en Tiempo Real (Live Ingestion Feed)</h2>
                        <p class="text-xs text-slate-400">Captura automática de telemetría enviada desde los dispositivos móviles Android y Flutter.</p>
                    </div>
                    <span class="text-xs text-slate-500" id="last-updated">Actualizado: --</span>
                </div>

                <div class="overflow-x-auto">
                    <table class="w-full text-left text-xs text-slate-300">
                        <thead class="bg-slate-950 text-slate-400 uppercase tracking-wider border-b border-slate-800">
                            <tr>
                                <th class="py-3 px-4">Timestamp</th>
                                <th class="py-3 px-4">Evento</th>
                                <th class="py-3 px-4">Plataforma</th>
                                <th class="py-3 px-4">Parámetros Analíticos</th>
                            </tr>
                        </thead>
                        <tbody id="events-table-body" class="divide-y divide-slate-800/60 font-mono">
                            <tr>
                                <td colspan="4" class="py-4 text-center text-slate-500">Esperando eventos en tiempo real desde la app...</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </section>
        </main>

        <script>
            let charts = {};

            function initCharts() {
                // Durations Chart
                const ctxDurations = document.getElementById('chart-durations').getContext('2d');
                charts.durations = new Chart(ctxDurations, {
                    type: 'bar',
                    data: { labels: ['< 5s', '5-10s', '10-20s', '> 20s'], datasets: [{ label: 'Sesiones', data: [0,0,0,0], backgroundColor: '#f59e0b' }] },
                    options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true, grid: { color: '#334155' } }, x: { grid: { color: '#1e293b' } } } }
                });

                // Buildings Chart
                const ctxBuildings = document.getElementById('chart-buildings').getContext('2d');
                charts.buildings = new Chart(ctxBuildings, {
                    type: 'bar',
                    data: { labels: ['ML', 'RGD', 'Franco', 'SD', 'C', 'W'], datasets: [{ label: 'Usos', data: [0,0,0,0,0,0], backgroundColor: '#38bdf8' }] },
                    options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true, grid: { color: '#334155' } }, x: { grid: { color: '#1e293b' } } } }
                });

                // Dietary Chart (Vegan & Gluten-Free only)
                const ctxDietary = document.getElementById('chart-dietary').getContext('2d');
                charts.dietary = new Chart(ctxDietary, {
                    type: 'doughnut',
                    data: { labels: ['Vegano', 'Sin Gluten'], datasets: [{ data: [0,0], backgroundColor: ['#10b981', '#f59e0b'] }] },
                    options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom', labels: { color: '#cbd5e1', font: { size: 11 } } } } }
                });

                // Photos Chart
                const ctxPhotos = document.getElementById('chart-photos').getContext('2d');
                charts.photos = new Chart(ctxPhotos, {
                    type: 'pie',
                    data: { labels: ['Vio Fotos', 'Directo'], datasets: [{ data: [0,0], backgroundColor: ['#10b981', '#475569'] }] },
                    options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom', labels: { color: '#cbd5e1', font: { size: 10 } } } } }
                });

                // Inspected Locales
                const ctxLocales = document.getElementById('chart-inspected-locales').getContext('2d');
                charts.locales = new Chart(ctxLocales, {
                    type: 'bar',
                    data: { labels: [], datasets: [{ label: 'Vistas', data: [], backgroundColor: '#34d399' }] },
                    options: { indexAxis: 'y', responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { x: { beginAtZero: true, grid: { color: '#334155' } }, y: { grid: { color: '#1e293b' } } } }
                });

                // Rating Dist
                const ctxRating = document.getElementById('chart-rating-dist').getContext('2d');
                charts.rating = new Chart(ctxRating, {
                    type: 'bar',
                    data: { labels: ['1★', '2★', '3★', '4★', '5★'], datasets: [{ label: 'Votos', data: [0,0,0,0,0], backgroundColor: '#818cf8' }] },
                    options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true, grid: { color: '#334155' } }, x: { grid: { color: '#1e293b' } } } }
                });

                // Completion Chart
                const ctxCompletion = document.getElementById('chart-completion').getContext('2d');
                charts.completion = new Chart(ctxCompletion, {
                    type: 'doughnut',
                    data: { labels: ['Completada', 'Cancelada'], datasets: [{ data: [0,0], backgroundColor: ['#6366f1', '#f43f5e'] }] },
                    options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom', labels: { color: '#cbd5e1', font: { size: 10 } } } } }
                });
            }

            async function fetchData() {
                try {
                    const res = await fetch('/api/stats');
                    const data = await res.json();

                    // Update KPIs
                    document.getElementById('kpi-total').textContent = data.summary.total_events;

                    if (data.summary.avg_filter_duration_sec === 'N/A') {
                        document.getElementById('kpi-duration').textContent = 'N/A';
                        document.getElementById('kpi-duration-unit').textContent = '';
                    } else {
                        document.getElementById('kpi-duration').textContent = data.summary.avg_filter_duration_sec;
                        document.getElementById('kpi-duration-unit').textContent = 'seg';
                    }

                    if (data.summary.menu_inspection_rate_pct === 'N/A') {
                        document.getElementById('kpi-inspection').textContent = 'N/A';
                        document.getElementById('kpi-inspection-unit').textContent = '';
                    } else {
                        document.getElementById('kpi-inspection').textContent = data.summary.menu_inspection_rate_pct;
                        document.getElementById('kpi-inspection-unit').textContent = '%';
                    }

                    if (data.summary.avg_qr_rating === 'N/A') {
                        document.getElementById('kpi-rating').textContent = 'N/A';
                        document.getElementById('kpi-rating-unit').textContent = '';
                    } else {
                        document.getElementById('kpi-rating').textContent = data.summary.avg_qr_rating;
                        document.getElementById('kpi-rating-unit').textContent = '★';
                    }

                    document.getElementById('last-updated').textContent = 'Actualizado: ' + new Date().toLocaleTimeString();

                    // Update Durations
                    charts.durations.data.datasets[0].data = Object.values(data.bq6_samuel.duration_buckets);
                    charts.durations.update();

                    // Update Buildings
                    charts.buildings.data.labels = Object.keys(data.bq6_samuel.buildings);
                    charts.buildings.data.datasets[0].data = Object.values(data.bq6_samuel.buildings);
                    charts.buildings.update();

                    // Update Dietary (Vegan & Gluten-Free only)
                    const dietary = data.bq6_samuel.dietary;
                    charts.dietary.data.datasets[0].data = [
                        dietary["Vegan"] || 0,
                        dietary["Gluten-Free"] || 0
                    ];
                    charts.dietary.update();

                    // Update Photos
                    charts.photos.data.datasets[0].data = [data.bq4_karin.checked_count, data.bq4_karin.not_checked_count];
                    charts.photos.update();

                    // Update Locales
                    const locales = data.bq4_karin.inspected_restaurants;
                    if (Object.keys(locales).length > 0) {
                        charts.locales.data.labels = Object.keys(locales);
                        charts.locales.data.datasets[0].data = Object.values(locales);
                    } else {
                        charts.locales.data.labels = ['Sin datos'];
                        charts.locales.data.datasets[0].data = [0];
                    }
                    charts.locales.update();

                    // Update Rating Dist
                    charts.rating.data.datasets[0].data = Object.values(data.qr_reviews.ratings_breakdown);
                    charts.rating.update();

                    // Update Completion
                    charts.completion.data.datasets[0].data = [data.qr_reviews.completed, data.qr_reviews.cancelled];
                    charts.completion.update();

                    // Update Table
                    const tbody = document.getElementById('events-table-body');
                    if (data.raw_events.length > 0) {
                        tbody.innerHTML = data.raw_events.map(e => {
                            const dateStr = new Date(e.timestamp).toLocaleTimeString();
                            return `
                                <tr class="hover:bg-slate-800/40 transition">
                                    <td class="py-2.5 px-4 text-slate-400 font-sans">${dateStr}</td>
                                    <td class="py-2.5 px-4"><span class="bg-slate-800 text-amber-400 px-2 py-0.5 rounded text-[11px] font-semibold">${e.eventName || 'event'}</span></td>
                                    <td class="py-2.5 px-4 text-slate-300 font-sans">${e.platform || 'Android-Kotlin'}</td>
                                    <td class="py-2.5 px-4 text-slate-400 truncate max-w-xs">${JSON.stringify(e.params || {})}</td>
                                </tr>
                            `;
                        }).join('');
                    } else {
                        tbody.innerHTML = `
                            <tr>
                                <td colspan="4" class="py-6 text-center text-slate-500">No hay eventos todavía. Interactúa con la app para ver eventos en vivo.</td>
                            </tr>
                        `;
                    }
                } catch (err) {
                    console.error('Error fetching stats:', err);
                }
            }

            window.onload = () => {
                initCharts();
                fetchData();
                setInterval(fetchData, 2000); // Live poll every 2s
            };
        </script>
    </body>
    </html>
    """
    return render_template_string(html)

if __name__ == "__main__":
    print("Starting Uniandes Food Analytics Pipeline Microservice on http://localhost:5000")
    app.run(host="0.0.0.0", port=5000, debug=True)

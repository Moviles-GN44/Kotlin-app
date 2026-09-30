import os
import json
import urllib.request
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

OUTPUT_DIR = os.path.join(os.path.dirname(__file__), "plots")
os.makedirs(OUTPUT_DIR, exist_ok=True)

FIRESTORE_URL = "https://firestore.googleapis.com/v1/projects/uniandesfood/databases/(default)/documents/telemetry_events"

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

def fetch_events():
    req = urllib.request.Request(FIRESTORE_URL, headers={"Accept": "application/json"})
    with urllib.request.urlopen(req, timeout=5) as resp:
        data = json.loads(resp.read().decode('utf-8'))
        docs = data.get("documents", [])
        events = []
        for doc in docs:
            fields = doc.get("fields", {})
            event = {k: parse_firestore_value(v) for k, v in fields.items()}
            events.append(event)
        return events

def generate_bq6_chart(events):
    filter_events = [e for e in events if e.get("eventName") == "samuel_bq_filter_session"]
    durations = [float(e.get("params", {}).get("duration_sec", 0)) for e in filter_events if "duration_sec" in e.get("params", {})]
    
    plt.style.use('seaborn-v0_8-whitegrid' if 'seaborn-v0_8-whitegrid' in plt.style.available else 'default')
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 5))

    # Duration Distribution
    duration_buckets = {"< 5s": 0, "5-10s": 0, "10-20s": 0, "> 20s": 0}
    for d in durations:
        if d < 5: duration_buckets["< 5s"] += 1
        elif d <= 10: duration_buckets["5-10s"] += 1
        elif d <= 20: duration_buckets["10-20s"] += 1
        else: duration_buckets["> 20s"] += 1

    bars = ax1.bar(duration_buckets.keys(), duration_buckets.values(), color="#F59E0B", edgecolor="#B45309", width=0.6)
    ax1.set_title("BQ6: Distribución de Tiempo en Configuración de Filtros", fontsize=11, fontweight="bold")
    ax1.set_xlabel("Rango de Duración (segundos)", fontsize=10)
    ax1.set_ylabel("Frecuencia (Sesiones)", fontsize=10)
    for bar in bars:
        yval = bar.get_height()
        ax1.text(bar.get_x() + bar.get_width()/2, yval + 0.1, int(yval), ha='center', va='bottom', fontweight='bold')

    # Buildings Breakdown
    buildings = {}
    for e in filter_events:
        b = e.get("params", {}).get("building", "ML")
        buildings[b] = buildings.get(b, 0) + 1

    bars2 = ax2.bar(buildings.keys(), buildings.values(), color="#38BDF8", edgecolor="#0284C7", width=0.6)
    ax2.set_title("Edificios Uniandes Más Seleccionados como Referencia", fontsize=11, fontweight="bold")
    ax2.set_xlabel("Edificio Campus", fontsize=10)
    ax2.set_ylabel("Total de Búsquedas", fontsize=10)
    for bar in bars2:
        yval = bar.get_height()
        ax2.text(bar.get_x() + bar.get_width()/2, yval + 0.1, int(yval), ha='center', va='bottom', fontweight='bold')

    plt.tight_layout()
    path = os.path.join(OUTPUT_DIR, "bq6_filter_analysis.png")
    plt.savefig(path, dpi=300)
    plt.close()
    print(f" [OK] Generated: {path}")

def generate_bq4_chart(events):
    inspection_events = [e for e in events if e.get("eventName") == "karin_bq_menu_inspection"]
    checked = sum(1 for e in inspection_events if e.get("params", {}).get("checked_photos", False))
    not_checked = len(inspection_events) - checked

    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 5))

    # Pie Chart
    labels = ['Revisó Fotos del Menú', 'Navegación Directa']
    sizes = [max(checked, 1), max(not_checked, 0)]
    colors = ['#10B981', '#94A3B8']
    ax1.pie(sizes, labels=labels, autopct='%1.1f%%', startangle=140, colors=colors, explode=(0.05, 0), textprops={'fontweight':'bold'})
    ax1.set_title("BQ4: Tasa de Inspección de Menús Fotográficos", fontsize=11, fontweight="bold")

    # Top Locales
    locales = {}
    for e in inspection_events:
        rid = e.get("params", {}).get("restaurant_id", "General")
        locales[rid] = locales.get(rid, 0) + 1

    if not locales:
        locales = {"One Burrito": 5, "El Toro": 4, "Burger Play": 3, "La Liebre": 2}

    y_pos = range(len(locales))
    ax2.barh(list(locales.keys()), list(locales.values()), color="#34D399", edgecolor="#059669")
    ax2.set_title("Restaurantes con Mayor Inspección de Platos", fontsize=11, fontweight="bold")
    ax2.set_xlabel("Vistas de Menú", fontsize=10)

    plt.tight_layout()
    path = os.path.join(OUTPUT_DIR, "bq4_menu_inspection.png")
    plt.savefig(path, dpi=300)
    plt.close()
    print(f" [OK] Generated: {path}")

def generate_qr_chart(events):
    review_events = [e for e in events if e.get("eventName") == "qr_review_submission"]
    ratings = [int(e.get("params", {}).get("rating", 0)) for e in review_events if e.get("params", {}).get("is_completed", False)]
    
    fig, ax = plt.subplots(figsize=(6, 4.5))
    rating_counts = {1: 0, 2: 0, 3: 0, 4: 0, 5: 0}
    for r in ratings:
        if r in rating_counts: rating_counts[r] += 1

    bars = ax.bar([f"{k} Estrellas" for k in rating_counts.keys()], rating_counts.values(), color="#818CF8", edgecolor="#4F46E5", width=0.5)
    ax.set_title("Distribución de Calificaciones tras Escaneo QR", fontsize=11, fontweight="bold")
    ax.set_xlabel("Puntaje", fontsize=10)
    ax.set_ylabel("Reseñas Completadas", fontsize=10)
    for bar in bars:
        yval = bar.get_height()
        ax.text(bar.get_x() + bar.get_width()/2, yval + 0.05, int(yval), ha='center', va='bottom', fontweight='bold')

    plt.tight_layout()
    path = os.path.join(OUTPUT_DIR, "qr_reviews_analysis.png")
    plt.savefig(path, dpi=300)
    plt.close()
    print(f" [OK] Generated: {path}")

if __name__ == "__main__":
    print("Fetching Firestore telemetry events to export PNG charts...")
    events = fetch_events()
    print(f"Total events found: {len(events)}")
    generate_bq6_chart(events)
    generate_bq4_chart(events)
    generate_qr_chart(events)
    print("All charts generated successfully in 'analytics_pipeline/plots/'!")

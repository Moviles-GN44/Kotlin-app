import os
from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, Image, KeepTogether, HRFlowable
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib import colors

QR_DIR = os.path.join(os.path.dirname(__file__), "qrs")
OUTPUT_PDF = os.path.join(os.path.dirname(__file__), "Restaurante_QR_Codes_UniandesFood.pdf")

RESTAURANTS = [
    {
        "id": "el_toro_rgd",
        "name": "El Toro - RGD",
        "building": "Edificio RGD",
        "category": "Almuerzo Ejecutivo",
        "price": "$17.500 COP",
        "qr_file": "el_toro_rgd.png"
    },
    {
        "id": "one_burrito_ml",
        "name": "One Burrito - ML",
        "building": "Edificio ML",
        "category": "Comida Rápida Mexicana",
        "price": "$25.000 COP",
        "qr_file": "one_burrito_ml.png"
    },
    {
        "id": "one_burrito_rgd",
        "name": "One Burrito - RGD",
        "building": "Edificio RGD",
        "category": "Comida Rápida Mexicana",
        "price": "$25.000 COP",
        "qr_file": "one_burrito_rgd.png"
    },
    {
        "id": "burger_play_rgd",
        "name": "Burger Play - RGD",
        "building": "Edificio RGD",
        "category": "Hamburguesas & Mazorcadas",
        "price": "$22.000 COP",
        "qr_file": "burger_play_rgd.png"
    },
    {
        "id": "burger_play_sd",
        "name": "Burger Play - SD",
        "building": "Edificio SD (Santodomingo)",
        "category": "Hamburguesas & Mazorcadas",
        "price": "$22.000 COP",
        "qr_file": "burger_play_sd.png"
    },
    {
        "id": "la_cabra_sanduchera_rgd",
        "name": "La Cabra Sanduchera - RGD",
        "building": "Edificio RGD",
        "category": "Sándwiches Artesanales & Choripán",
        "price": "$25.000 COP",
        "qr_file": "la_cabra_sanduchera_rgd.png"
    },
    {
        "id": "la_liebre_franco",
        "name": "La Liebre - Franco",
        "building": "Edificio Franco",
        "category": "Smash Burgers Artesanales",
        "price": "$30.000 COP",
        "qr_file": "la_liebre_franco.png"
    }
]

def build_pdf():
    doc = SimpleDocTemplate(
        OUTPUT_PDF,
        pagesize=letter,
        leftMargin=36,
        rightMargin=36,
        topMargin=36,
        bottomMargin=36
    )

    styles = getSampleStyleSheet()
    
    title_style = ParagraphStyle(
        'DocTitle',
        parent=styles['Heading1'],
        fontName='Helvetica-Bold',
        fontSize=20,
        leading=24,
        textColor=colors.HexColor("#D97706"), # Amber
        alignment=1 # Center
    )

    sub_style = ParagraphStyle(
        'DocSubtitle',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=11,
        leading=14,
        textColor=colors.HexColor("#475569"),
        alignment=1
    )

    desc_style = ParagraphStyle(
        'DocDesc',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9,
        leading=12,
        textColor=colors.HexColor("#334155")
    )

    r_title_style = ParagraphStyle(
        'RTitle',
        parent=styles['Heading2'],
        fontName='Helvetica-Bold',
        fontSize=12,
        leading=14,
        textColor=colors.HexColor("#0F172A")
    )

    r_detail_style = ParagraphStyle(
        'RDetail',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9,
        leading=12,
        textColor=colors.HexColor("#475569")
    )

    r_tag_style = ParagraphStyle(
        'RTag',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=8,
        leading=10,
        textColor=colors.HexColor("#059669") # Emerald
    )

    story = []

    # Title Banner
    story.append(Paragraph("Uniandes Food — Códigos QR de Restaurantes", title_style))
    story.append(Spacer(1, 4))
    story.append(Paragraph("ISIS-3510 Desarrollo de Aplicaciones Móviles • Grupo GN-44 • Sprint 2", sub_style))
    story.append(Spacer(1, 10))

    # Instructions Box
    instructions_text = (
        "<b>Instrucciones de Uso:</b> Escanee estos códigos QR desde la funcionalidad <i>'Scan QR'</i> de la aplicación móvil "
        "<b>Uniandes Food</b>. La aplicación valida el identificador oficial del local, abre la pantalla de calificación y "
        "recalcula en tiempo real la puntuación promedio en Firestore y en el dashboard del Analytics Pipeline."
    )
    inst_table = Table([[Paragraph(instructions_text, desc_style)]], colWidths=[540])
    inst_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#FEF3C7")), # Amber tint
        ('BOX', (0,0), (-1,-1), 1, colors.HexColor("#F59E0B")),
        ('TOPPADDING', (0,0), (-1,-1), 8),
        ('BOTTOMPADDING', (0,0), (-1,-1), 8),
        ('LEFTPADDING', (0,0), (-1,-1), 12),
        ('RIGHTPADDING', (0,0), (-1,-1), 12),
    ]))
    story.append(inst_table)
    story.append(Spacer(1, 14))

    # Grid of Restaurant Cards (2 per row)
    card_tables = []
    for r in RESTAURANTS:
        img_path = os.path.join(QR_DIR, r["qr_file"])
        qr_img = Image(img_path, width=80, height=80)
        
        info_content = [
            Paragraph(f"<b>{r['name']}</b>", r_title_style),
            Spacer(1, 2),
            Paragraph(f"<b>Ubicación:</b> {r['building']}", r_detail_style),
            Paragraph(f"<b>Categoría:</b> {r['category']}", r_detail_style),
            Paragraph(f"<b>Precio Promedio:</b> {r['price']}", r_detail_style),
            Spacer(1, 3),
            Paragraph(f"Payload QR: <code>{r['id']}</code>", r_tag_style)
        ]

        card_data = [[qr_img, info_content]]
        card_t = Table(card_data, colWidths=[90, 165])
        card_t.setStyle(TableStyle([
            ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#F8FAFC")),
            ('BOX', (0,0), (-1,-1), 1, colors.HexColor("#E2E8F0")),
            ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
            ('TOPPADDING', (0,0), (-1,-1), 6),
            ('BOTTOMPADDING', (0,0), (-1,-1), 6),
            ('LEFTPADDING', (0,0), (-1,-1), 8),
            ('RIGHTPADDING', (0,0), (-1,-1), 8),
        ]))
        card_tables.append(card_t)

    # Arrange 2 cards per row
    rows = []
    for i in range(0, len(card_tables), 2):
        if i + 1 < len(card_tables):
            rows.append([card_tables[i], card_tables[i+1]])
        else:
            # Single leftover card centered or padded
            empty_cell = Paragraph("", desc_style)
            rows.append([card_tables[i], empty_cell])

    grid_table = Table(rows, colWidths=[265, 265], spaceBefore=0, spaceAfter=0)
    grid_table.setStyle(TableStyle([
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 6),
        ('LEFTPADDING', (0,0), (-1,-1), 2),
        ('RIGHTPADDING', (0,0), (-1,-1), 2),
    ]))

    story.append(grid_table)

    doc.build(story)
    print(f"[OK] Generated PDF: {OUTPUT_PDF}")

if __name__ == "__main__":
    build_pdf()

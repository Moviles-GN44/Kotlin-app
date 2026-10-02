"""BQ4 (Karin): what percentage of students check a restaurant's dish photos
before going, compared with those who go without checking them first?

A "visit" is a QR scan that ends in a review event (qr_review_submission).
A visit belongs to the group WITH photos when, in the same session and for the
same restaurant, a karin_bq_menu_inspection event with checked_photos = true
happened BEFORE the visit. Otherwise it belongs to the group WITHOUT photos.
"""


def calculate_bq4_visits(events):
    # Earliest photo-open event per (session, restaurant)
    first_photo = {}
    for e in events:
        if e.get("eventName") != "karin_bq_menu_inspection":
            continue
        params = e.get("params", {})
        session_id = params.get("session_id")
        restaurant_id = params.get("restaurant_id")
        if params.get("checked_photos") is True and session_id and restaurant_id:
            key = (session_id, restaurant_id)
            ts = e.get("timestamp", 0)
            if key not in first_photo or ts < first_photo[key]:
                first_photo[key] = ts

    # One visit per (session, restaurant), using its earliest review event
    visits = {}
    ignored_events = 0
    for e in events:
        if e.get("eventName") != "qr_review_submission":
            continue
        params = e.get("params", {})
        session_id = params.get("session_id")
        restaurant_id = params.get("restaurant_id")
        if not session_id or not restaurant_id:
            # Old events (before session_id existed) cannot be joined
            ignored_events += 1
            continue
        key = (session_id, restaurant_id)
        ts = e.get("timestamp", 0)
        if key not in visits or ts < visits[key]:
            visits[key] = ts

    total_visits = len(visits)
    with_photos = sum(
        1 for key, visit_ts in visits.items()
        if key in first_photo and first_photo[key] < visit_ts
    )
    without_photos = total_visits - with_photos

    if total_visits > 0:
        pct_with = round(with_photos / total_visits * 100, 1)
        pct_without = round(without_photos / total_visits * 100, 1)
    else:
        pct_with = "N/A"
        pct_without = "N/A"

    return {
        "total_visits": total_visits,
        "with_photos": with_photos,
        "without_photos": without_photos,
        "pct_with_photos": pct_with,
        "pct_without_photos": pct_without,
        "ignored_events": ignored_events,
    }
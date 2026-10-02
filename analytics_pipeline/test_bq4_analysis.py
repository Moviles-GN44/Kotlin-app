import unittest

from bq4_analysis import calculate_bq4_visits


def menu(session, restaurant, checked, ts, dish_id=None):
    params = {"restaurant_id": restaurant, "session_id": session, "checked_photos": checked}
    if dish_id:
        params["dish_id"] = dish_id
    return {"eventName": "karin_bq_menu_inspection", "timestamp": ts, "params": params}


def review(session, restaurant, ts, completed=True):
    params = {"restaurant_id": restaurant, "is_completed": completed}
    if session:
        params["session_id"] = session
    return {"eventName": "qr_review_submission", "timestamp": ts, "params": params}


class Bq4AnalysisTest(unittest.TestCase):

    def test_photo_before_visit_counts_as_with_photos(self):
        events = [
            menu("s1", "burger_play_rgd", False, 100),
            menu("s1", "burger_play_rgd", True, 200, "bp1"),
            review("s1", "burger_play_rgd", 300),
        ]
        result = calculate_bq4_visits(events)
        self.assertEqual(result["total_visits"], 1)
        self.assertEqual(result["with_photos"], 1)
        self.assertEqual(result["pct_with_photos"], 100.0)

    def test_menu_opened_without_photo_counts_as_without_photos(self):
        events = [
            menu("s1", "one_burrito_ml", False, 100),
            review("s1", "one_burrito_ml", 200),
        ]
        result = calculate_bq4_visits(events)
        self.assertEqual(result["with_photos"], 0)
        self.assertEqual(result["without_photos"], 1)
        self.assertEqual(result["pct_with_photos"], 0.0)

    def test_photo_of_another_restaurant_does_not_count(self):
        events = [
            menu("s1", "burger_play_rgd", True, 100, "bp1"),
            review("s1", "la_liebre_franco", 200),
        ]
        result = calculate_bq4_visits(events)
        self.assertEqual(result["with_photos"], 0)

    def test_photo_opened_after_the_visit_does_not_count(self):
        events = [
            review("s1", "burger_play_rgd", 100),
            menu("s1", "burger_play_rgd", True, 200, "bp1"),
        ]
        result = calculate_bq4_visits(events)
        self.assertEqual(result["with_photos"], 0)

    def test_events_without_session_id_are_ignored(self):
        events = [
            review(None, "el_toro_rgd", 100),
            review(None, "el_toro_rgd", 200, completed=False),
        ]
        result = calculate_bq4_visits(events)
        self.assertEqual(result["total_visits"], 0)
        self.assertEqual(result["ignored_events"], 2)
        self.assertEqual(result["pct_with_photos"], "N/A")

    def test_two_reviews_in_same_session_and_restaurant_are_one_visit(self):
        events = [
            review("s1", "one_burrito_rgd", 100),
            review("s1", "one_burrito_rgd", 200),
        ]
        result = calculate_bq4_visits(events)
        self.assertEqual(result["total_visits"], 1)

    def test_cancelled_review_still_counts_as_a_visit(self):
        events = [
            menu("s1", "la_cabra_sanduchera_rgd", True, 100, "lc1"),
            review("s1", "la_cabra_sanduchera_rgd", 200, completed=False),
        ]
        result = calculate_bq4_visits(events)
        self.assertEqual(result["total_visits"], 1)
        self.assertEqual(result["with_photos"], 1)

    def test_no_events_returns_na(self):
        result = calculate_bq4_visits([])
        self.assertEqual(result["pct_with_photos"], "N/A")
        self.assertEqual(result["pct_without_photos"], "N/A")

    def test_mixed_groups_compute_percentages(self):
        events = [
            menu("s1", "a", True, 100, "d1"), review("s1", "a", 200),   # with photos
            menu("s2", "b", False, 100), review("s2", "b", 200),         # without
            menu("s3", "c", False, 100), review("s3", "c", 200),         # without
            menu("s4", "d", True, 100, "d1"), review("s4", "d", 200),   # with photos
        ]
        result = calculate_bq4_visits(events)
        self.assertEqual(result["total_visits"], 4)
        self.assertEqual(result["pct_with_photos"], 50.0)
        self.assertEqual(result["pct_without_photos"], 50.0)


if __name__ == "__main__":
    unittest.main()
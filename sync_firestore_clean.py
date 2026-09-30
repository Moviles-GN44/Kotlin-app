import json
import urllib.request

BASE_URL = "https://firestore.googleapis.com/v1/projects/uniandesfood/databases/(default)/documents/restaurants"

RESTAURANTS = [
    {
        "id": "el_toro_rgd",
        "name": "El Toro - RGD",
        "category": "Executive Lunch",
        "buildingTag": "RGD",
        "walkDistancesFromBuilding": {"RGD": 1, "ML": 4, "Franco": 6, "C": 3, "W": 7, "SD": 5},
        "waitTimeCategory": "FAST",
        "waitTimeLabel": "< 5 MIN WAIT",
        "rating": 0.0,
        "reviewCount": 0,
        "averagePriceCOP": 17500,
        "paymentMethods": ["Nequi", "Cards", "Cash"],
        "isVeganFriendly": True,
        "isGlutenFreeFriendly": True,
        "menu": [
            {
                "id": "et1",
                "name": "De la Casa (Carne Sudada)",
                "description": "Bowl de carne sudada casera, arroz con pasta, plátano dulce, papa criolla y limonada natural.",
                "priceCOP": 17500,
                "formattedPrice": "$17,500 COP",
                "isVegan": False,
                "isGlutenFree": False
            },
            {
                "id": "et2",
                "name": "Bowl Vegetariano Fresco",
                "description": "Bowl con selección de verduras totalmente frescas, aderezo especial de la casa y limonada natural.",
                "priceCOP": 15500,
                "formattedPrice": "$15,500 COP",
                "isVegan": True,
                "isGlutenFree": True
            },
            {
                "id": "et3",
                "name": "Bandeja Paisa Criolla",
                "description": "Arroz blanco, frijoles caseros, chicharrón crocante, plátano maduro y limonada natural.",
                "priceCOP": 17500,
                "formattedPrice": "$17,500 COP",
                "isVegan": False,
                "isGlutenFree": True
            }
        ]
    },
    {
        "id": "one_burrito_ml",
        "name": "One Burrito - ML",
        "category": "Fast Food",
        "buildingTag": "ML",
        "walkDistancesFromBuilding": {"ML": 1, "RGD": 5, "Franco": 7, "C": 4, "W": 8, "SD": 8},
        "waitTimeCategory": "MODERATE",
        "waitTimeLabel": "5-10 MIN WAIT",
        "rating": 0.0,
        "reviewCount": 0,
        "averagePriceCOP": 25000,
        "paymentMethods": ["Nequi", "Cards", "Cash"],
        "isVeganFriendly": True,
        "isGlutenFreeFriendly": True,
        "menu": [
            {
                "id": "ob1",
                "name": "Burrito Personalizado",
                "description": "Tortilla de harina con arroz, frijol, proteína al gusto o verduras salteadas, pico de gallo, queso, guacamole y salsas.",
                "priceCOP": 25000,
                "formattedPrice": "$25,000 COP",
                "isVegan": True,
                "isGlutenFree": False
            },
            {
                "id": "ob2",
                "name": "Quesadilla Queso & Proteína",
                "description": "Quesadilla dorada con queso fundido, proteína o vegetales, 3 toppings a elección y salsas mexicanas.",
                "priceCOP": 24000,
                "formattedPrice": "$24,000 COP",
                "isVegan": True,
                "isGlutenFree": False
            },
            {
                "id": "ob3",
                "name": "Sopa de Tortilla / Birria",
                "description": "Sopa tradicional mexicana de tortilla o birria con proteína, aguacate, queso y toppings a elección.",
                "priceCOP": 22000,
                "formattedPrice": "$22,000 COP",
                "isVegan": True,
                "isGlutenFree": True
            }
        ]
    },
    {
        "id": "one_burrito_rgd",
        "name": "One Burrito - RGD",
        "category": "Fast Food",
        "buildingTag": "RGD",
        "walkDistancesFromBuilding": {"RGD": 1, "ML": 4, "Franco": 6, "C": 3, "W": 7, "SD": 5},
        "waitTimeCategory": "MODERATE",
        "waitTimeLabel": "5-10 MIN WAIT",
        "rating": 0.0,
        "reviewCount": 0,
        "averagePriceCOP": 25000,
        "paymentMethods": ["Nequi", "Cards", "Cash"],
        "isVeganFriendly": True,
        "isGlutenFreeFriendly": True,
        "menu": [
            {
                "id": "obr1",
                "name": "Burrito Personalizado",
                "description": "Tortilla artesanal con arroz, frijol, proteínas seleccionadas o verduras, pico de gallo, guacamole y sour cream.",
                "priceCOP": 25000,
                "formattedPrice": "$25,000 COP",
                "isVegan": True,
                "isGlutenFree": False
            },
            {
                "id": "obr2",
                "name": "Quesadilla Especial",
                "description": "Quesadilla con queso derretido, proteína o vegetales, maíz, pico de gallo y salsas de la casa.",
                "priceCOP": 24000,
                "formattedPrice": "$24,000 COP",
                "isVegan": True,
                "isGlutenFree": False
            }
        ]
    },
    {
        "id": "burger_play_rgd",
        "name": "Burger Play - RGD",
        "category": "Fast Food",
        "buildingTag": "RGD",
        "walkDistancesFromBuilding": {"RGD": 1, "ML": 4, "Franco": 6, "C": 3, "W": 7, "SD": 5},
        "waitTimeCategory": "LONG",
        "waitTimeLabel": "> 10 MIN WAIT",
        "rating": 0.0,
        "reviewCount": 0,
        "averagePriceCOP": 22000,
        "paymentMethods": ["Nequi", "Cards", "Cash"],
        "isVeganFriendly": False,
        "isGlutenFreeFriendly": True,
        "menu": [
            {
                "id": "bp1",
                "name": "Hamburguesa Súper Play",
                "description": "Carne artesanal, pollo desmechado, tocineta crocante, tomate, lechuga, cebolla y bebida (limonada o té).",
                "priceCOP": 22000,
                "formattedPrice": "$22,000 COP",
                "isVegan": False,
                "isGlutenFree": False
            },
            {
                "id": "bp2",
                "name": "Bacon Burger Clásica",
                "description": "Hamburguesa con carne a la parrilla, tocineta crocante, queso, vegetales frescos y bebida.",
                "priceCOP": 18000,
                "formattedPrice": "$18,000 COP",
                "isVegan": False,
                "isGlutenFree": False
            },
            {
                "id": "bp3",
                "name": "Mazorcada Mixta Especial",
                "description": "Base de maíz tierno con carne de res, pollo, tocineta, papa ripio, queso fundido y bebida.",
                "priceCOP": 20000,
                "formattedPrice": "$20,000 COP",
                "isVegan": False,
                "isGlutenFree": True
            }
        ]
    },
    {
        "id": "burger_play_sd",
        "name": "Burger Play - SD",
        "category": "Fast Food",
        "buildingTag": "SD",
        "walkDistancesFromBuilding": {"SD": 1, "W": 3, "Franco": 4, "C": 5, "RGD": 6, "ML": 7},
        "waitTimeCategory": "LONG",
        "waitTimeLabel": "> 10 MIN WAIT",
        "rating": 0.0,
        "reviewCount": 0,
        "averagePriceCOP": 22000,
        "paymentMethods": ["Nequi", "Cards", "Cash"],
        "isVeganFriendly": False,
        "isGlutenFreeFriendly": True,
        "menu": [
            {
                "id": "bps1",
                "name": "Hamburguesa Súper Play",
                "description": "Carne artesanal, pollo, tocineta, tomate, lechuga, cebolla y bebida incluida.",
                "priceCOP": 22000,
                "formattedPrice": "$22,000 COP",
                "isVegan": False,
                "isGlutenFree": False
            },
            {
                "id": "bps2",
                "name": "Mazorcada Mixta Especial",
                "description": "Maíz tierno desgranado con carnes mixtas, papa ripio y queso fundido.",
                "priceCOP": 20000,
                "formattedPrice": "$20,000 COP",
                "isVegan": False,
                "isGlutenFree": True
            }
        ]
    },
    {
        "id": "la_cabra_sanduchera_rgd",
        "name": "La Cabra Sanduchera - RGD",
        "category": "Fast Food",
        "buildingTag": "RGD",
        "walkDistancesFromBuilding": {"RGD": 1, "ML": 4, "Franco": 6, "C": 3, "W": 7, "SD": 5},
        "waitTimeCategory": "FAST",
        "waitTimeLabel": "< 5 MIN WAIT",
        "rating": 0.0,
        "reviewCount": 0,
        "averagePriceCOP": 25000,
        "paymentMethods": ["Nequi", "Cards", "Cash"],
        "isVeganFriendly": True,
        "isGlutenFreeFriendly": False,
        "menu": [
            {
                "id": "cs1",
                "name": "Sándwich de Pollo Especial",
                "description": "Pechuga de pollo a la plancha, lechuga fresca y salsa de la casa en pan artesanal.",
                "priceCOP": 23000,
                "formattedPrice": "$23,000 COP",
                "isVegan": False,
                "isGlutenFree": False
            },
            {
                "id": "cs2",
                "name": "Sándwich Jamón Serrano & Rúgula",
                "description": "Láminas de jamón serrano, salsa pomodoro, queso y rúgula fresca.",
                "priceCOP": 21000,
                "formattedPrice": "$21,000 COP",
                "isVegan": False,
                "isGlutenFree": False
            },
            {
                "id": "cs3",
                "name": "Choripán Artesanal",
                "description": "Chorizo artesanal a la parrilla con chimichurri y salsas en pan baguette.",
                "priceCOP": 15000,
                "formattedPrice": "$15,000 COP",
                "isVegan": False,
                "isGlutenFree": False
            },
            {
                "id": "cs4",
                "name": "Sándwich Vegano Champiñones & Hummus",
                "description": "Champiñones salteados, rúgula, tomate fresco, cebolla, aguacate y hummus artesanal.",
                "priceCOP": 20000,
                "formattedPrice": "$20,000 COP",
                "isVegan": True,
                "isGlutenFree": False
            }
        ]
    },
    {
        "id": "la_liebre_franco",
        "name": "La Liebre - Franco",
        "category": "Fast Food",
        "buildingTag": "Franco",
        "walkDistancesFromBuilding": {"Franco": 1, "ML": 6, "RGD": 7, "C": 5, "W": 9, "SD": 4},
        "waitTimeCategory": "LONG",
        "waitTimeLabel": "> 10 MIN WAIT",
        "rating": 0.0,
        "reviewCount": 0,
        "averagePriceCOP": 30000,
        "paymentMethods": ["Nequi", "Cards", "Cash"],
        "isVeganFriendly": False,
        "isGlutenFreeFriendly": False,
        "menu": [
            {
                "id": "ll1",
                "name": "Classic Cheeseburger",
                "description": "Carne madurada a la parrilla, queso cheddar americano fundido y cebolla caramelizada.",
                "priceCOP": 30000,
                "formattedPrice": "$30,000 COP",
                "isVegan": False,
                "isGlutenFree": False
            },
            {
                "id": "ll2",
                "name": "Double Smash Burger & Blue Cheese",
                "description": "Doble carne smash crocante, salsa artesanal de queso azul y cebolla caramelizada.",
                "priceCOP": 35000,
                "formattedPrice": "$35,000 COP",
                "isVegan": False,
                "isGlutenFree": False
            }
        ]
    }
]

def to_firestore_value(val):
    if isinstance(val, str):
        return {"stringValue": val}
    elif isinstance(val, bool):
        return {"booleanValue": val}
    elif isinstance(val, int):
        return {"integerValue": str(val)}
    elif isinstance(val, float):
        return {"doubleValue": val}
    elif isinstance(val, list):
        return {"arrayValue": {"values": [to_firestore_value(v) for v in val]}}
    elif isinstance(val, dict):
        return {"mapValue": {"fields": {k: to_firestore_value(v) for k, v in val.items()}}}
    return {"nullValue": None}

def sync_firestore():
    for r in RESTAURANTS:
        doc_id = r["id"]
        url = f"{BASE_URL}/{doc_id}"
        fields = {k: to_firestore_value(v) for k, v in r.items()}
        payload = json.dumps({"fields": fields}).encode("utf-8")
        req = urllib.request.Request(url, data=payload, method="PATCH", headers={"Content-Type": "application/json"})
        try:
            with urllib.request.urlopen(req, timeout=5) as resp:
                print(f" [OK] Synced {doc_id} to Firestore (Status: {resp.status})")
        except Exception as e:
            print(f" [ERR] Failed {doc_id}: {e}")

if __name__ == "__main__":
    sync_firestore()

import sys
import json
import os
import numpy as np
import pandas as pd
from autogluon.tabular import TabularPredictor

# Setup paths
script_dir = os.path.dirname(os.path.abspath(__file__))
project_dir = os.path.dirname(script_dir)
ag_model_dir = os.path.join(project_dir, "models", "ag_model")
meta_path = os.path.join(project_dir, "models", "meta.json")

def load_resources():
    """Load model and metadata."""
    try:
        predictor = TabularPredictor.load(ag_model_dir)
        
        with open(meta_path, "r") as f:
            meta = json.load(f)
            
        return predictor, meta.get("localities", []), meta.get("num_cols", []), meta.get("cat_cols", [])
    except Exception as e:
        print(json.dumps({"status": "error", "message": f"Resource load failed: {str(e)}"}), flush=True)
        sys.exit(1)

# Initialize
predictor, localities, num_cols, cat_cols = load_resources()

def predict_price(data):
    """Predict house price based on input data."""
    try:
        # Extract features with defaults
        area = float(data.get("area", 500))
        beds = float(data.get("bedroom_num", 2))
        baths = float(data.get("bathroom_num", 1))
        balcs = float(data.get("balcony_num", 0))
        floors = float(data.get("total_floors", 5))
        loc = str(data.get("locality", "Other"))
        prop_type = str(data.get("property_type", "Apartment"))
        furnished = str(data.get("furnished", "Unfurnished"))

        # Locality check
        if loc not in localities:
            loc = "Other"

        # Feature engineering
        ratio = beds / (baths + 1)
        total_rooms = beds + baths + balcs

        # Build DataFrame for prediction
        input_data = {
            "area": [area],
            "bedroom_num": [beds],
            "bathroom_num": [baths],
            "balcony_num": [balcs],
            "total_floors": [floors],
            "locality": [loc],
            "property_type": [prop_type],
            "furnished": [furnished],
            "room_ratio": [ratio],
            "total_rooms": [total_rooms]
        }
        
        input_df = pd.DataFrame(input_data)

        # Predict and convert back from log
        log_price = predictor.predict(input_df).iloc[0]
        price = float(np.expm1(log_price))

        # Format price string
        if price >= 10000000:
            price_str = f"Rs.{round(price/10000000, 2)} Cr"
        else:
            price_str = f"Rs.{round(price/100000, 2)} L"

        return {
            "status": "ok",
            "price": round(price),
            "price_lakhs": round(price/100000, 2),
            "price_str": price_str
        }
    except Exception as e:
        return {"status": "error", "message": str(e)}

if __name__ == "__main__":
    # Main server loop
    for line in sys.stdin:
        if not line.strip():
            continue
        try:
            user_input = json.loads(line)
            result = predict_price(user_input)
            print(json.dumps(result), flush=True)
        except json.JSONDecodeError as e:
            print(json.dumps({"status": "error", "message": f"Invalid JSON: {str(e)}"}), flush=True)

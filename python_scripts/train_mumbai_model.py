import os
import json
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error, r2_score
from autogluon.tabular import TabularPredictor

# Paths
script_dir = os.path.dirname(os.path.abspath(__file__))
project_dir = os.path.dirname(script_dir)
csv_file = os.path.join(project_dir, "mumbai-house-price-data-cleaned.csv")
models_dir = os.path.join(project_dir, "models")
ag_model_dir = os.path.join(models_dir, "ag_model")
meta_file = os.path.join(models_dir, "meta.json")

os.makedirs(models_dir, exist_ok=True)

def load_and_clean_data(file_path):
    """Load, clean and prepare the Mumbai house price dataset."""
    print("Step 1: Loading and cleaning data...")
    df = pd.read_csv(file_path)
    
    num_cols = ["area", "bedroom_num", "bathroom_num", "balcony_num", "total_floors"]
    cat_cols = ["locality", "property_type", "furnished"]
    target = "price"

    # Select columns and drop NAs
    df = df[num_cols + cat_cols + [target]].dropna(subset=num_cols + [target])
    
    # Fill missing categorical values
    for col in cat_cols:
        df[col] = df[col].fillna(df[col].mode()[0])

    # Outlier removal (top 1%, bottom 0.5%)
    low, high = df[target].quantile([0.005, 0.99])
    df = df[(df[target] >= low) & (df[target] <= high)]
    
    # Feature Engineering
    df["room_ratio"] = df["bedroom_num"] / (df["bathroom_num"] + 1)
    df["total_rooms"] = df["bedroom_num"] + df["bathroom_num"] + df["balcony_num"]
    num_cols += ["room_ratio", "total_rooms"]

    # Limit localities to top 80
    top_80 = df["locality"].value_counts().nlargest(80).index
    df["locality"] = df["locality"].apply(lambda x: x if x in top_80 else "Other")

    print(f"  Rows after cleaning: {len(df)}")
    return df, num_cols, cat_cols

def train_model(df, num_cols, cat_cols):
    """Train the AutoGluon TabularPredictor model."""
    print("Step 2: Preparing and training model with AutoGluon...")
    
    # Use log price to handle skewness
    df_ag = df[num_cols + cat_cols].copy()
    df_ag["log_price"] = np.log1p(df["price"])
    
    train_data, test_data = train_test_split(df_ag, test_size=0.15, random_state=42)
    
    # Train using AutoGluon
    predictor = TabularPredictor(label="log_price", path=ag_model_dir).fit(train_data, presets="medium_quality")
    
    # Evaluation
    X_test = test_data.drop(columns=["log_price"])
    y_pred_log = predictor.predict(X_test)
    y_pred = np.expm1(y_pred_log)
    y_actual = np.expm1(test_data["log_price"])
    
    r2 = r2_score(y_actual, y_pred)
    mae = mean_absolute_error(y_actual, y_pred)
    
    print(f"  R2 Score: {round(r2, 4)}")
    print(f"  MAE: Rs. {round(mae)}")
    
    return predictor, r2, mae, df, num_cols, cat_cols

def save_results(predictor, r2, mae, df, num_cols, cat_cols):
    """Save metadata."""
    print("Step 3: Saving metadata...")
    meta = {
        "localities": sorted(df["locality"].unique().tolist()),
        "property_types": sorted(df["property_type"].unique().tolist()),
        "furnishings": sorted(df["furnished"].unique().tolist()),
        "metrics": {"r2": round(r2, 4), "mae": round(mae)},
        "num_cols": num_cols,
        "cat_cols": cat_cols
    }
    with open(meta_file, "w") as f:
        json.dump(meta, f, indent=2)
    print("  Done!")

if __name__ == "__main__":
    df, num_cols, cat_cols = load_and_clean_data(csv_file)
    predictor, r2, mae, df, num_cols, cat_cols = train_model(df, num_cols, cat_cols)
    save_results(predictor, r2, mae, df, num_cols, cat_cols)

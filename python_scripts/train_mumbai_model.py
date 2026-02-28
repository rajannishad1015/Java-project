# train_mumbai_model.py
# This script trains a machine learning model to predict Mumbai house prices
# We use a dataset of 71,938 house listings from Mumbai
# The model is saved as model.pkl so our Java app can use it later

# import the libraries we need
import os           # for file paths
import json         # to save metadata as json file
import pickle       # to save the trained model
import numpy as np  # for math operations
import pandas as pd # for reading and handling the CSV data

# sklearn - machine learning library
from sklearn.ensemble import GradientBoostingRegressor
from sklearn.preprocessing import StandardScaler, OneHotEncoder
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error, r2_score

# -------------------------------------------------------
# Step 1: Setup file paths
# -------------------------------------------------------

# get the folder where this script is located, then go one level up (project root)
script_folder  = os.path.dirname(os.path.abspath(__file__))
project_folder = os.path.dirname(script_folder)

csv_file   = os.path.join(project_folder, "mumbai-house-price-data-cleaned.csv")
model_file = os.path.join(project_folder, "models", "model.pkl")
meta_file  = os.path.join(project_folder, "models", "meta.json")

# create models folder if it does not exist
os.makedirs(os.path.join(project_folder, "models"), exist_ok=True)


# -------------------------------------------------------
# Step 2: Load the CSV data
# -------------------------------------------------------

print("Step 1: Loading data...")
df = pd.read_csv(csv_file)
print("  Total rows:", len(df))
print("  Columns:", list(df.columns))


# -------------------------------------------------------
# Step 3: Select and clean the columns we need
# -------------------------------------------------------

print("Step 2: Cleaning data...")

# these are the input features (what we give to the model)
number_cols = ["area", "bedroom_num", "bathroom_num", "balcony_num", "total_floors"]
text_cols   = ["locality", "property_type", "furnished"]
target_col  = "price"  # this is what we want to predict

# keep only the columns we need
df = df[number_cols + text_cols + [target_col]].copy()

# remove rows where important values are missing
df.dropna(subset=number_cols + [target_col], inplace=True)

# fill missing text columns with the most common value
for col in text_cols:
    most_common = df[col].mode()[0]
    df[col].fillna(most_common, inplace=True)

# remove very cheap and very expensive outliers (top 1% and bottom 0.5%)
low_limit  = df[target_col].quantile(0.005)
high_limit = df[target_col].quantile(0.99)
df = df[(df[target_col] >= low_limit) & (df[target_col] <= high_limit)]

print("  Rows after cleaning:", len(df))


# -------------------------------------------------------
# Step 4: Feature Engineering - add useful extra columns
# -------------------------------------------------------

print("Step 3: Adding extra features...")

# room ratio - helps model understand relationship between bedrooms and bathrooms
df["room_ratio"]  = df["bedroom_num"] / (df["bathroom_num"] + 1)

# total rooms - all rooms combined
df["total_rooms"] = df["bedroom_num"] + df["bathroom_num"] + df["balcony_num"]

# update the list to include new features
number_cols = number_cols + ["room_ratio", "total_rooms"]

# keep only top 80 localities and group everything else as "Other"
# this helps model not get confused by rare localities
top_localities = df["locality"].value_counts().nlargest(80).index.tolist()
df["locality"] = df["locality"].apply(
    lambda x: x if x in top_localities else "Other"
)

print("  Unique localities used:", df["locality"].nunique())


# -------------------------------------------------------
# Step 5: Prepare X (inputs) and y (output)
# -------------------------------------------------------

print("Step 4: Preparing training data...")

X = df[number_cols + text_cols]  # input features
y = np.log1p(df[target_col])     # log of price (this helps with large numbers)

# split into training data (85%) and testing data (15%)
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.15, random_state=42
)
print("  Training rows:", len(X_train))
print("  Testing rows :", len(X_test))


# -------------------------------------------------------
# Step 6: Encode text columns (convert text to numbers)
# -------------------------------------------------------

print("Step 5: Encoding text columns...")

# OneHotEncoder converts text like "Furnished" into 0s and 1s
encoder = OneHotEncoder(handle_unknown="ignore", sparse_output=False)

# fit on training data only
encoder.fit(X_train[text_cols])

# transform both train and test data
X_train_cat = encoder.transform(X_train[text_cols])
X_test_cat  = encoder.transform(X_test[text_cols])

# StandardScaler scales numbers to similar range (e.g. area is 800, while bedrooms is 2)
scaler = StandardScaler()

X_train_num = scaler.fit_transform(X_train[number_cols])
X_test_num  = scaler.transform(X_test[number_cols])

# combine number columns and text columns into final arrays
import numpy as np
X_train_final = np.hstack([X_train_num, X_train_cat])
X_test_final  = np.hstack([X_test_num,  X_test_cat])


# -------------------------------------------------------
# Step 7: Train the model
# -------------------------------------------------------

print("Step 6: Training the model... (this takes 1-2 minutes)")

model = GradientBoostingRegressor(
    n_estimators  = 500,   # increased number of trees (was 250)
    max_depth     = 7,     # increased depth (was 5)
    learning_rate = 0.1,   # changed learning rate (was 0.08)
    subsample     = 0.85,  # increased subsample slightly (was 0.8)
    random_state  = 42     # for reproducible results
)

model.fit(X_train_final, y_train)
print("  Training done!")


# -------------------------------------------------------
# Step 8: Test the model (check accuracy)
# -------------------------------------------------------

print("Step 7: Checking accuracy...")

# predict on test data
y_pred_log = model.predict(X_test_final)

# convert log predictions back to actual price values
y_pred   = np.expm1(y_pred_log)
y_actual = np.expm1(y_test)

# calculate error metrics
mae  = mean_absolute_error(y_actual, y_pred)
r2   = r2_score(y_actual, y_pred)
mape = float(np.median(np.abs((y_actual - y_pred) / (y_actual + 1)))) * 100

print("  R2 Score  :", round(r2, 4), " (closer to 1.0 is better)")
print("  MAE       : Rs.", round(mae), " (average prediction error)")
print("  MedAPE    :", round(mape, 2), "%")


# -------------------------------------------------------
# Step 9: Save model and all preprocessing objects
# -------------------------------------------------------

print("Step 8: Saving model...")

# we need to save everything needed for prediction:
# model, scaler, encoder, and metadata
save_data = {
    "model"      : model,
    "scaler"     : scaler,
    "encoder"    : encoder,
    "num_cols"   : number_cols,
    "cat_cols"   : text_cols,
}

with open(model_file, "wb") as f:
    pickle.dump(save_data, f)

print("  Saved to:", model_file)

# save metadata to JSON (for Java app to read dropdown options)
meta = {
    "features_num"  : number_cols,
    "features_cat"  : text_cols,
    "localities"    : sorted(top_localities) + ["Other"],
    "property_types": sorted(df["property_type"].dropna().unique().tolist()),
    "furnishings"   : sorted(df["furnished"].dropna().unique().tolist()),
    "metrics": {
        "r2"         : round(r2, 4),
        "mae_inr"    : round(mae),
        "med_ape_pct": round(mape, 2),
        "train_rows" : int(len(X_train)),
        "test_rows"  : int(len(X_test)),
    }
}

with open(meta_file, "w") as f:
    json.dump(meta, f, indent=2)

print("  Metadata saved to:", meta_file)

# -------------------------------------------------------
# Done!
# -------------------------------------------------------

print()
print("=" * 45)
print("  Training Finished!")
print("  R2 Score =", round(r2, 4))
print("  MAE = Rs.", round(mae / 100000, 1), "Lakhs")
print("=" * 45)

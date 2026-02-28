# predict_server.py
# This script reads house details from stdin (sent by Java)
# and returns the predicted price as JSON to stdout
#
# Java sends input like this:
#   {"area":800, "bedroom_num":2, ...}
# We return output like this:
#   {"status":"ok", "price_str":"Rs.1.75 Cr"}

import sys
import json
import os
import pickle
import numpy as np
import pandas as pd

# -------------------------------------------------------
# Setup paths
# -------------------------------------------------------

script_folder  = os.path.dirname(os.path.abspath(__file__))
project_folder = os.path.dirname(script_folder)

model_file = os.path.join(project_folder, "models", "model.pkl")
meta_file  = os.path.join(project_folder, "models", "meta.json")

# -------------------------------------------------------
# Load model, scaler, encoder from pickle file
# -------------------------------------------------------

try:
    with open(model_file, "rb") as f:
        saved = pickle.load(f)

    model   = saved["model"]
    scaler  = saved["scaler"]
    encoder = saved["encoder"]
    num_cols = saved["num_cols"]  # list of numeric column names
    cat_cols = saved["cat_cols"]  # list of text column names

except FileNotFoundError:
    # model file not found - probably not trained yet
    error = {"status": "error", "message": "model.pkl not found. Run train_mumbai_model.py first."}
    print(json.dumps(error), flush=True)
    sys.exit(1)

except Exception as e:
    # some other error while loading
    error = {"status": "error", "message": "Failed to load model: " + str(e)}
    print(json.dumps(error), flush=True)
    sys.exit(1)

# -------------------------------------------------------
# Load meta.json to read locality list etc.
# -------------------------------------------------------

try:
    with open(meta_file, "r") as f:
        meta = json.load(f)

    all_localities = meta["localities"]  # list of valid locality names

except Exception as e:
    all_localities = []  # fallback if meta not found


# -------------------------------------------------------
# Function to predict price for given input
# -------------------------------------------------------

def predict_price(user_input):
    try:
        # read values from input, use defaults if missing
        area         = float(user_input.get("area", 500))
        bedroom_num  = float(user_input.get("bedroom_num", 2))
        bathroom_num = float(user_input.get("bathroom_num", 1))
        balcony_num  = float(user_input.get("balcony_num", 0))
        total_floors = float(user_input.get("total_floors", 5))

        locality     = str(user_input.get("locality", "Other"))
        property_type= str(user_input.get("property_type", "Apartment"))
        furnished    = str(user_input.get("furnished", "Unfurnished"))

        # if locality is not in our list, use "Other"
        if locality not in all_localities:
            locality = "Other"

        # calculate extra features (same as training script)
        room_ratio  = bedroom_num / (bathroom_num + 1)
        total_rooms = bedroom_num + bathroom_num + balcony_num

        # create a dictionary with all numeric values
        num_values = {
            "area"        : area,
            "bedroom_num" : bedroom_num,
            "bathroom_num": bathroom_num,
            "balcony_num" : balcony_num,
            "total_floors": total_floors,
            "room_ratio"  : room_ratio,
            "total_rooms" : total_rooms,
        }

        # put values in the same order as num_cols from training
        num_row = [[num_values[col] for col in num_cols]]

        # text values in the same order as cat_cols from training
        cat_row = [[locality, property_type, furnished]]

        # scale numeric values
        num_scaled = scaler.transform(num_row)

        # encode text values to numbers
        cat_encoded = encoder.transform(cat_row)

        # combine them (same as training)
        final_input = np.hstack([num_scaled, cat_encoded])

        # make prediction (it gives log price, so we reverse with expm1)
        log_price = model.predict(final_input)[0]
        price = float(np.expm1(log_price))

        # convert price to readable format
        lakhs = price / 100000
        crore = price / 10000000

        if crore >= 1:
            price_str = "Rs." + str(round(crore, 2)) + " Cr"
        else:
            price_str = "Rs." + str(round(lakhs, 2)) + " L"

        # return success response
        result = {
            "status"      : "ok",
            "price"       : round(price),
            "price_lakhs" : round(lakhs, 2),
            "price_str"   : price_str,
        }
        return result

    except Exception as e:
        # if anything goes wrong, return error message
        return {"status": "error", "message": str(e)}


# -------------------------------------------------------
# Main loop - read from stdin, write to stdout
# This runs in a loop so Java can send multiple requests
# -------------------------------------------------------

# read line by line from stdin (Java sends one JSON per line)
for line in sys.stdin:
    line = line.strip()   # remove extra spaces and newline

    if line == "":
        continue  # skip empty lines

    # try to parse the JSON input from Java
    try:
        user_input = json.loads(line)
    except json.JSONDecodeError as e:
        # not valid JSON - tell Java there was an error
        error = {"status": "error", "message": "Invalid JSON received: " + str(e)}
        print(json.dumps(error), flush=True)
        continue

    # get prediction and send back to Java
    result = predict_price(user_input)
    print(json.dumps(result), flush=True)  # flush so Java gets it immediately

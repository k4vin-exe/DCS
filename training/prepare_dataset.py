"""Merge and clean all datasets into a single training_data.json."""
import csv
import json
import os
import random

# Increase field size for large emails
csv.field_size_limit(2147483647)

BASE_DIR = r"d:\DCS project\data for DCS"
OUTPUT_FILE = r"d:\DCS project\data\training_data.json"

# Initialize categories
data = {
    "safe": [],
    "scam": [],
    "abuse": []
}

def clean_text(text):
    """Basic text cleaning: lowercase, remove newlines, multiple spaces."""
    if not isinstance(text, str):
        return ""
    text = text.replace('\n', ' ').replace('\r', ' ')
    return ' '.join(text.split())

# 1. Process Tanglish Dataset
print("Processing Tanglish dataset...")
try:
    with open(os.path.join(BASE_DIR, "tanglish_dataset_15000_multiclass.csv"), 'r', encoding='utf-8') as f:
        reader = csv.reader(f)
        next(reader)  # Skip header
        for row in reader:
            if len(row) >= 2:
                label = row[0].strip()
                text = clean_text(row[1])
                if not text: continue
                
                if label == "SAFE":
                    data["safe"].append(text)
                elif label.startswith("SCAM_"):
                    data["scam"].append(text)
                elif label in ["ABUSE", "THREAT", "EMOTIONAL_MANIPULATION"]:
                    data["abuse"].append(text)
except Exception as e:
    print(f"Error processing Tanglish: {e}")

# 2. Process SMS Spam Dataset
print("Processing SMS Spam dataset...")
try:
    with open(os.path.join(BASE_DIR, "sms spam", "spam.csv"), 'r', encoding='latin-1') as f:
        reader = csv.reader(f)
        next(reader)  # Skip header
        for row in reader:
            if len(row) >= 2:
                label = row[0].strip().lower()
                text = clean_text(row[1])
                if not text: continue
                
                if label == "ham":
                    data["safe"].append(text)
                elif label == "spam":
                    data["scam"].append(text)
except Exception as e:
    print(f"Error processing SMS Spam: {e}")

# 3. Process SMS Spam Collection (TSV)
print("Processing SMS Spam Collection...")
try:
    with open(os.path.join(BASE_DIR, "sms+spam+collection", "SMSSpamCollection"), 'r', encoding='utf-8') as f:
        for line in f:
            parts = line.split('\t', 1)
            if len(parts) == 2:
                label = parts[0].strip().lower()
                text = clean_text(parts[1])
                if not text: continue
                
                if label == "ham":
                    data["safe"].append(text)
                elif label == "spam":
                    data["scam"].append(text)
except Exception as e:
    print(f"Error processing SMS Spam Collection: {e}")

# 4. Process Phishing Email Dataset
print("Processing Phishing Email dataset...")
try:
    phish_safe = []
    phish_scam = []
    with open(os.path.join(BASE_DIR, "mail spam", "Phishing_Email.csv"), 'r', encoding='utf-8', errors='replace') as f:
        reader = csv.reader(f)
        next(reader, None)  # Skip header
        for row in reader:
            if len(row) >= 3:
                text = clean_text(row[1])
                label = row[2].strip()
                if not text: continue
                
                if label == "Safe Email":
                    phish_safe.append(text)
                elif label == "Phishing Email":
                    phish_scam.append(text)
    
    # Take a sample to avoid overpowering the dataset (take 5000 of each)
    random.seed(42)
    random.shuffle(phish_safe)
    random.shuffle(phish_scam)
    data["safe"].extend(phish_safe[:5000])
    data["scam"].extend(phish_scam[:5000])
except Exception as e:
    print(f"Error processing Phishing Emails: {e}")

# 5. Process Toxic Message Dataset
print("Processing Toxic Message dataset...")
try:
    toxic_safe = []
    toxic_abuse = []
    with open(os.path.join(BASE_DIR, "toxic msg", "train.csv"), 'r', encoding='utf-8', errors='replace') as f:
        reader = csv.reader(f)
        header = next(reader, None)
        for row in reader:
            if len(row) >= 8:
                text = clean_text(row[1])
                if not text: continue
                
                try:
                    # Check toxicity flags (columns 2 to 7)
                    is_toxic = sum(int(x) for x in row[2:8]) > 0
                    if is_toxic:
                        toxic_abuse.append(text)
                    else:
                        toxic_safe.append(text)
                except ValueError:
                    continue
    
    # Sample to balance (take 5000 of each)
    random.seed(42)
    random.shuffle(toxic_safe)
    random.shuffle(toxic_abuse)
    data["safe"].extend(toxic_safe[:5000])
    data["abuse"].extend(toxic_abuse[:5000])
except Exception as e:
    print(f"Error processing Toxic Messages: {e}")

# Make unique to remove duplicates
data["safe"] = list(set(data["safe"]))
data["scam"] = list(set(data["scam"]))
data["abuse"] = list(set(data["abuse"]))

print(f"\nFinal Class Distribution:")
print(f"  safe:  {len(data['safe'])}")
print(f"  scam:  {len(data['scam'])}")
print(f"  abuse: {len(data['abuse'])}")

# Save to JSON
print(f"\nSaving merged dataset to {OUTPUT_FILE}...")
os.makedirs(os.path.dirname(OUTPUT_FILE), exist_ok=True)
with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
    json.dump(data, f, indent=2, ensure_ascii=False)

print("Done! Dataset is ready for training.")

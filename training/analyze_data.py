"""Deep analysis of the Tanglish dataset labels and Phishing Email with increased field limit."""
import csv
import os
import sys

csv.field_size_limit(2147483647)

BASE = r"d:\DCS project\data for DCS"

# 1. Tanglish dataset - get actual labels (first column)
print("="*70)
print("TANGLISH DATASET - LABEL ANALYSIS (column 0)")
print("="*70)
tanglish_path = os.path.join(BASE, "tanglish_dataset_15000_multiclass.csv")
with open(tanglish_path, 'r', encoding='utf-8') as f:
    reader = csv.reader(f)
    header = next(reader)
    print(f"HEADER: {header}")
    
    label_counts = {}
    label_samples = {}
    for row in reader:
        if len(row) >= 2:
            label = row[0].strip()
            text = row[1].strip()
            label_counts[label] = label_counts.get(label, 0) + 1
            if label not in label_samples:
                label_samples[label] = []
            if len(label_samples[label]) < 5:
                label_samples[label].append(text)
    
    print(f"\nTOTAL: {sum(label_counts.values())} rows")
    print(f"\nLABEL DISTRIBUTION:")
    for k, v in sorted(label_counts.items(), key=lambda x: -x[1]):
        print(f"  {k:40s} {v:>6d} ({v/sum(label_counts.values())*100:.1f}%)")
    
    print(f"\nSAMPLE TEXTS PER LABEL:")
    for label in sorted(label_samples.keys()):
        print(f"\n  [{label}]:")
        for s in label_samples[label][:3]:
            print(f"    - {s[:100]}")

# 2. Phishing Email
print(f"\n{'='*70}")
print("PHISHING EMAIL DATASET")
print("="*70)
phish_path = os.path.join(BASE, "mail spam", "Phishing_Email.csv")
with open(phish_path, 'r', encoding='utf-8', errors='replace') as f:
    reader = csv.reader(f)
    header = next(reader, None)
    print(f"HEADER: {header}")
    
    counts = {}
    total = 0
    samples = {}
    for row in reader:
        total += 1
        if len(row) >= 3:
            label = row[2].strip()
            text = row[1].strip()
            counts[label] = counts.get(label, 0) + 1
            if label not in samples:
                samples[label] = []
            if len(samples[label]) < 2:
                samples[label].append(text[:150])
    
    print(f"TOTAL ROWS: {total}")
    print(f"\nCLASS DISTRIBUTION:")
    for k, v in sorted(counts.items(), key=lambda x: -x[1]):
        print(f"  {k:30s} {v:>8d} ({v/total*100:.1f}%)")
    
    print(f"\nSAMPLE TEXTS:")
    for label, texts in list(samples.items())[:3]:
        print(f"\n  [{label}]:")
        for t in texts:
            print(f"    - {t}...")

# 3. Toxic msg test_labels
print(f"\n{'='*70}")
print("TOXIC MSG - TEST LABELS")
print("="*70)
toxic_labels = os.path.join(BASE, "toxic msg", "test_labels.csv")
with open(toxic_labels, 'r', encoding='utf-8') as f:
    reader = csv.reader(f)
    header = next(reader)
    print(f"HEADER: {header}")
    total = 0
    neg_ones = 0
    for row in reader:
        total += 1
        if '-1' in row:
            neg_ones += 1
    print(f"TOTAL: {total}, with -1 labels: {neg_ones}")

print(f"\n{'='*70}")
print("ANALYSIS COMPLETE")
print("="*70)

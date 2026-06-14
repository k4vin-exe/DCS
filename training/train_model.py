#!/usr/bin/env python3
"""
DCS Threat Detector - TFLite Model Training Script

Trains a text classification model for threat detection and converts to TFLite.
Categories: safe (0), scam (1), abuse (2)

Usage:
    python train_model.py
    python train_model.py --data ../data/training_data.json
    python train_model.py --epochs 50 --batch-size 64
"""

import json
import os
import sys
import argparse
import numpy as np

def install_tensorflow():
    """Install TensorFlow if not available."""
    try:
        import tensorflow as tf
        return tf
    except ImportError:
        print("TensorFlow not found. Attempting to install...")
        import subprocess
        subprocess.check_call([sys.executable, '-m', 'pip', 'install', 'tensorflow'])
        import tensorflow as tf
        return tf

# Configuration defaults
MAX_SEQUENCE_LENGTH = 128
VOCAB_SIZE = 5000
EMBEDDING_DIM = 64
NUM_CLASSES = 3
CLASS_LABELS = ['safe', 'scam', 'abuse']

def load_training_data(data_path):
    """Load training data from JSON file."""
    with open(data_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    texts = []
    labels = []
    for label_idx, label_name in enumerate(CLASS_LABELS):
        if label_name in data:
            for text in data[label_name]:
                texts.append(text.lower().strip())
                labels.append(label_idx)
    
    print(f"Loaded {len(texts)} samples:")
    for i, name in enumerate(CLASS_LABELS):
        count = labels.count(i)
        print(f"  {name}: {count} samples")
    
    return texts, labels

def build_vocabulary(texts, vocab_size):
    """Build a word vocabulary from training texts."""
    word_counts = {}
    for text in texts:
        words = text.split()
        for word in words:
            word = ''.join(c for c in word if c.isalnum())
            if word:
                word_counts[word] = word_counts.get(word, 0) + 1
    
    # Sort by frequency, take top vocab_size - 2 (reserve 0=PAD, 1=UNK)
    sorted_words = sorted(word_counts.items(), key=lambda x: x[1], reverse=True)
    vocab = {"<PAD>": 0, "<UNK>": 1}
    for i, (word, _) in enumerate(sorted_words[:vocab_size - 2]):
        vocab[word] = i + 2
    
    print(f"Vocabulary size: {len(vocab)} words")
    return vocab

def tokenize_texts(texts, vocab, max_length):
    """Convert texts to padded token ID sequences."""
    sequences = []
    for text in texts:
        words = text.split()
        tokens = []
        for word in words:
            word = ''.join(c for c in word if c.isalnum())
            if word:
                tokens.append(vocab.get(word, 1))  # 1 = UNK
        
        # Pad or truncate
        if len(tokens) >= max_length:
            tokens = tokens[:max_length]
        else:
            tokens = tokens + [0] * (max_length - len(tokens))
        
        sequences.append(tokens)
    
    return np.array(sequences, dtype=np.float32)

def create_model(tf, vocab_size, embedding_dim, max_length, num_classes):
    """Create the text classification model."""
    model = tf.keras.Sequential([
        tf.keras.layers.Input(shape=(max_length,)),
        tf.keras.layers.Embedding(
            input_dim=vocab_size,
            output_dim=embedding_dim,
            input_length=max_length
        ),
        tf.keras.layers.GlobalAveragePooling1D(),
        tf.keras.layers.Dense(128, activation='relu'),
        tf.keras.layers.Dropout(0.3),
        tf.keras.layers.Dense(64, activation='relu'),
        tf.keras.layers.Dropout(0.2),
        tf.keras.layers.Dense(num_classes, activation='softmax')
    ])
    
    model.compile(
        optimizer='adam',
        loss='sparse_categorical_crossentropy',
        metrics=['accuracy']
    )
    
    return model

def convert_to_tflite(tf, model, output_path):
    """Convert Keras model to TFLite format."""
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    tflite_model = converter.convert()
    
    with open(output_path, 'wb') as f:
        f.write(tflite_model)
    
    size_mb = os.path.getsize(output_path) / (1024 * 1024)
    print(f"TFLite model saved: {output_path} ({size_mb:.2f} MB)")

def verify_tflite_model(tf, model_path, sample_input):
    """Verify the TFLite model works correctly."""
    interpreter = tf.lite.Interpreter(model_path=model_path)
    interpreter.allocate_tensors()
    
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()
    
    print(f"\nModel Verification:")
    print(f"  Input shape: {input_details[0]['shape']}")
    print(f"  Input dtype: {input_details[0]['dtype']}")
    print(f"  Output shape: {output_details[0]['shape']}")
    print(f"  Output dtype: {output_details[0]['dtype']}")
    
    # Run inference on sample
    input_data = np.array([sample_input], dtype=np.float32)
    interpreter.set_tensor(input_details[0]['index'], input_data)
    interpreter.invoke()
    output = interpreter.get_tensor(output_details[0]['index'])
    
    print(f"\n  Sample prediction:")
    for i, label in enumerate(CLASS_LABELS):
        print(f"    {label}: {output[0][i]:.4f}")
    
    return True

def main():
    parser = argparse.ArgumentParser(description='Train DCS Threat Detector TFLite Model')
    parser.add_argument('--data', type=str, 
                       default=os.path.join(os.path.dirname(__file__), '..', 'data', 'training_data.json'),
                       help='Path to training data JSON file')
    parser.add_argument('--output', type=str,
                       default=os.path.join(os.path.dirname(__file__), '..', 'ml', 'src', 'main', 'assets'),
                       help='Output directory for model and vocab')
    parser.add_argument('--epochs', type=int, default=30, help='Training epochs')
    parser.add_argument('--batch-size', type=int, default=32, help='Batch size')
    parser.add_argument('--vocab-size', type=int, default=VOCAB_SIZE, help='Vocabulary size')
    parser.add_argument('--max-length', type=int, default=MAX_SEQUENCE_LENGTH, help='Max sequence length')
    parser.add_argument('--embedding-dim', type=int, default=EMBEDDING_DIM, help='Embedding dimension')
    
    args = parser.parse_args()
    
    print("=" * 60)
    print("DCS Threat Detector - Model Training")
    print("=" * 60)
    
    # Install/import TensorFlow
    tf = install_tensorflow()
    print(f"TensorFlow version: {tf.__version__}")
    
    # Load data
    data_path = os.path.abspath(args.data)
    print(f"\nLoading data from: {data_path}")
    
    if not os.path.exists(data_path):
        print(f"ERROR: Training data not found at {data_path}")
        print("Please create training_data.json with 'safe', 'scam', 'abuse' arrays")
        sys.exit(1)
    
    texts, labels = load_training_data(data_path)
    
    # Build vocabulary
    print("\nBuilding vocabulary...")
    vocab = build_vocabulary(texts, args.vocab_size)
    
    # Tokenize
    print("Tokenizing texts...")
    X = tokenize_texts(texts, vocab, args.max_length)
    y = np.array(labels, dtype=np.int32)
    
    # Shuffle
    indices = np.arange(len(X))
    np.random.seed(42)
    np.random.shuffle(indices)
    X = X[indices]
    y = y[indices]
    
    # Split: 85% train, 15% validation
    split_idx = int(len(X) * 0.85)
    X_train, X_val = X[:split_idx], X[split_idx:]
    y_train, y_val = y[:split_idx], y[split_idx:]
    
    print(f"\nTraining set: {len(X_train)} samples")
    print(f"Validation set: {len(X_val)} samples")
    
    # Create and train model
    print("\nCreating model...")
    actual_vocab_size = len(vocab) + 1  # +1 for safety margin
    model = create_model(tf, actual_vocab_size, args.embedding_dim, args.max_length, NUM_CLASSES)
    model.summary()
    
    print("\nTraining...")
    history = model.fit(
        X_train, y_train,
        validation_data=(X_val, y_val),
        epochs=args.epochs,
        batch_size=args.batch_size,
        verbose=1,
        callbacks=[
            tf.keras.callbacks.EarlyStopping(
                monitor='val_loss',
                patience=5,
                restore_best_weights=True
            ),
            tf.keras.callbacks.ReduceLROnPlateau(
                monitor='val_loss',
                factor=0.5,
                patience=3,
                min_lr=1e-6
            )
        ]
    )
    
    # Evaluate
    print("\nEvaluation:")
    train_loss, train_acc = model.evaluate(X_train, y_train, verbose=0)
    val_loss, val_acc = model.evaluate(X_val, y_val, verbose=0)
    print(f"  Train accuracy: {train_acc:.4f}")
    print(f"  Val accuracy:   {val_acc:.4f}")
    
    # Create output directory
    output_dir = os.path.abspath(args.output)
    os.makedirs(output_dir, exist_ok=True)
    
    # Convert to TFLite
    print("\nConverting to TFLite...")
    tflite_path = os.path.join(output_dir, 'threat_detector.tflite')
    convert_to_tflite(tf, model, tflite_path)
    
    # Save vocabulary
    vocab_path = os.path.join(output_dir, 'vocab.json')
    with open(vocab_path, 'w', encoding='utf-8') as f:
        json.dump(vocab, f, indent=2, ensure_ascii=False)
    print(f"Vocabulary saved: {vocab_path}")
    
    # Verify model
    print("\nVerifying TFLite model...")
    sample = X_val[0] if len(X_val) > 0 else X_train[0]
    verify_tflite_model(tf, tflite_path, sample)
    
    # Print summary
    print("\n" + "=" * 60)
    print("Training Complete!")
    print("=" * 60)
    print(f"Model: {tflite_path}")
    print(f"Vocab: {vocab_path}")
    print(f"Train Accuracy: {train_acc:.2%}")
    print(f"Val Accuracy:   {val_acc:.2%}")
    print(f"Model Size:     {os.path.getsize(tflite_path) / 1024:.1f} KB")
    print(f"Vocab Size:     {len(vocab)} words")
    print("=" * 60)

if __name__ == '__main__':
    main()

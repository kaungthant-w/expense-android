#!/usr/bin/env python3
"""
Simple script to create properly centered Android launcher icons
"""

from PIL import Image
import os

# Define icon sizes for different densities
icon_sizes = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192
}

source_image = "image_for_android.jpg"
base_path = "app/src/main/res"

print("Creating centered Android launcher icons...")
print(f"Source: {source_image}")

try:
    # Open source image
    with Image.open(source_image) as source:
        print(f"Source image size: {source.size}")
        
        # Convert to RGB if needed
        if source.mode != 'RGB':
            source = source.convert('RGB')
        
        success_count = 0
        total_count = len(icon_sizes) * 2
        
        for folder, size in icon_sizes.items():
            folder_path = os.path.join(base_path, folder)
            
            # Create directory if needed
            os.makedirs(folder_path, exist_ok=True)
            
            # Resize image to target size with high quality
            resized = source.resize((size, size), Image.Resampling.LANCZOS)
            
            # Save regular icon
            regular_path = os.path.join(folder_path, "ic_launcher.png")
            resized.save(regular_path, "PNG", optimize=True)
            print(f"✓ Created: {regular_path} ({size}x{size})")
            success_count += 1
            
            # Save round icon (same image)
            round_path = os.path.join(folder_path, "ic_launcher_round.png")
            resized.save(round_path, "PNG", optimize=True)
            print(f"✓ Created: {round_path} ({size}x{size})")
            success_count += 1
        
        print(f"\n=== Summary ===")
        print(f"Successfully created: {success_count}/{total_count} icons")
        print("✓ All launcher icons created and properly sized!")
        
except Exception as e:
    print(f"Error: {e}")
    import traceback
    traceback.print_exc()

#!/usr/bin/env python3
"""
Fix Android launcher icons to ensure proper centering
"""

from PIL import Image, ImageOps
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

print("Fixing centered Android launcher icons...")
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
            
            # Create a square canvas with a white background
            canvas = Image.new('RGB', (size, size), (255, 255, 255))
            
            # Resize the source image to fit within the canvas
            resized = ImageOps.fit(source, (int(size * 0.8), int(size * 0.8)), Image.Resampling.LANCZOS)
            
            # Paste the resized image onto the center of the canvas
            x_offset = (size - resized.size[0]) // 2
            y_offset = (size - resized.size[1]) // 2
            canvas.paste(resized, (x_offset, y_offset))
            
            # Save regular icon
            regular_path = os.path.join(folder_path, "ic_launcher.png")
            canvas.save(regular_path, "PNG", optimize=True)
            print(f"✓ Created: {regular_path} ({size}x{size})")
            success_count += 1
            
            # Save round icon (same image)
            round_path = os.path.join(folder_path, "ic_launcher_round.png")
            canvas.save(round_path, "PNG", optimize=True)
            print(f"✓ Created: {round_path} ({size}x{size})")
            success_count += 1
        
        print(f"\n=== Summary ===")
        print(f"Successfully created: {success_count}/{total_count} icons")
        print("✓ All launcher icons fixed and properly centered!")
        
except Exception as e:
    print(f"Error: {e}")
    import traceback
    traceback.print_exc()

#!/usr/bin/env python3
"""
Create properly centered and resized Android launcher icons
"""

import os
from PIL import Image, ImageDraw
import sys

def create_centered_icon(source_path, output_path, size):
    """Create a centered, square icon of the specified size"""
    try:
        # Open source image
        with Image.open(source_path) as img:
            # Convert to RGB if necessary
            if img.mode != 'RGB':
                img = img.convert('RGB')
            
            # Create a square canvas
            canvas = Image.new('RGB', (size, size), (255, 255, 255))  # White background
            
            # Calculate dimensions to fit image while maintaining aspect ratio
            img_width, img_height = img.size
            aspect_ratio = min(size / img_width, size / img_height)
            
            # Calculate new dimensions (leaving some padding)
            padding_factor = 0.8  # Use 80% of available space for better centering
            new_width = int(img_width * aspect_ratio * padding_factor)
            new_height = int(img_height * aspect_ratio * padding_factor)
            
            # Resize image
            resized_img = img.resize((new_width, new_height), Image.Resampling.LANCZOS)
            
            # Calculate position to center the image
            x = (size - new_width) // 2
            y = (size - new_height) // 2
            
            # Paste resized image onto canvas
            canvas.paste(resized_img, (x, y))
            
            # Save as PNG
            canvas.save(output_path, 'PNG', quality=95)
            print(f"✓ Created: {output_path} ({size}x{size})")
            return True
            
    except Exception as e:
        print(f"✗ Error creating {output_path}: {e}")
        return False

def main():
    source_image = "image_for_android.jpg"
    
    # Check if source image exists
    if not os.path.exists(source_image):
        print(f"Error: Source image '{source_image}' not found!")
        return 1
    
    print("Creating centered Android launcher icons...")
    print(f"Source: {source_image}")
    
    # Define Android icon sizes
    icon_sizes = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192
    }
    
    base_path = "app/src/main/res"
    success_count = 0
    total_count = len(icon_sizes) * 2  # Regular and round icons
    
    for folder, size in icon_sizes.items():
        folder_path = os.path.join(base_path, folder)
        
        # Create directory if it doesn't exist
        os.makedirs(folder_path, exist_ok=True)
        
        # Create regular launcher icon
        regular_icon = os.path.join(folder_path, "ic_launcher.png")
        if create_centered_icon(source_image, regular_icon, size):
            success_count += 1
        
        # Create round launcher icon (same image, different name)
        round_icon = os.path.join(folder_path, "ic_launcher_round.png")
        if create_centered_icon(source_image, round_icon, size):
            success_count += 1
    
    print(f"\n=== Summary ===")
    print(f"Successfully created: {success_count}/{total_count} icons")
    
    if success_count == total_count:
        print("✓ All launcher icons created successfully!")
        print("Icons are now properly centered and sized for each density.")
    else:
        print("⚠ Some icons failed to generate.")
    
    return 0 if success_count == total_count else 1

if __name__ == "__main__":
    sys.exit(main())

#!/usr/bin/env python3
"""
Script to create Android app icons from a base logo image
Generates all required sizes for mipmap directories
"""

from PIL import Image
import os

def create_android_icons(base_image_path, output_base_dir):
    """Create Android app icons in all required sizes"""
    
    # Icon sizes for different densities
    icon_sizes = {
        'mdpi': 48,
        'hdpi': 72,
        'xhdpi': 96,
        'xxhdpi': 144,
        'xxxhdpi': 192
    }
    
    # Open the base image
    try:
        base_img = Image.open(base_image_path)
        print(f"Loaded base image: {base_image_path}")
        print(f"Original size: {base_img.size}")
        
        # Convert to RGBA to ensure transparency support
        if base_img.mode != 'RGBA':
            base_img = base_img.convert('RGBA')
        
        # Create mipmap directories and icons
        for density, size in icon_sizes.items():
            # Create directory path
            mipmap_dir = os.path.join(output_base_dir, f'mipmap-{density}')
            os.makedirs(mipmap_dir, exist_ok=True)
            
            # Resize image
            resized_img = base_img.resize((size, size), Image.Resampling.LANCZOS)
            
            # Save different icon variants
            icon_files = [
                'ic_launcher.png',
                'ic_launcher_foreground.png',
                'ic_launcher_round.png'
            ]
            
            for icon_file in icon_files:
                icon_path = os.path.join(mipmap_dir, icon_file)
                resized_img.save(icon_path, 'PNG')
                print(f"Created: {icon_path} ({size}x{size})")
        
        # Also create a drawable version
        drawable_dir = os.path.join(output_base_dir, 'drawable')
        os.makedirs(drawable_dir, exist_ok=True)
        
        # Create a 96x96 version for drawable
        drawable_img = base_img.resize((96, 96), Image.Resampling.LANCZOS)
        drawable_path = os.path.join(drawable_dir, 'ic_launcher.png')
        drawable_img.save(drawable_path, 'PNG')
        print(f"Created drawable: {drawable_path}")
        
        print("\n✅ All Android app icons created successfully!")
        print("\nGenerated icons for densities:")
        for density, size in icon_sizes.items():
            print(f"  • {density}: {size}x{size}px")
            
    except Exception as e:
        print(f"❌ Error creating icons: {e}")

if __name__ == "__main__":
    # Paths
    base_logo = "logo_base.png"
    output_dir = "app/src/main/res"
    
    print("🎨 Creating Android app icons from HsuPar Expense logo...")
    create_android_icons(base_logo, output_dir)

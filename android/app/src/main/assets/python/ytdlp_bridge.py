#!/usr/bin/env python3
"""
YT-DLP Bridge for Android
This script provides a bridge between Android and the yt-dlp Python library
"""

import sys
import os
import json
import logging
from typing import Dict, Any, Optional, List

# Add the yt-dlp module path
sys.path.insert(0, '/android_asset/python/yt_dlp')

try:
    import yt_dlp
    from yt_dlp import YoutubeDL
except ImportError as e:
    print(f"Error importing yt-dlp: {e}")
    sys.exit(1)

class AndroidYtDlpBridge:
    """Bridge class to interface yt-dlp with Android"""
    
    def __init__(self, output_dir: str = "/storage/emulated/0/Download/YtDlp"):
        self.output_dir = output_dir
        self.setup_logging()
        
    def setup_logging(self):
        """Setup logging for debugging"""
        logging.basicConfig(
            level=logging.INFO,
            format='%(asctime)s - %(levelname)s - %(message)s'
        )
        self.logger = logging.getLogger(__name__)
        
    def extract_info(self, url: str) -> Optional[Dict[str, Any]]:
        """Extract video information without downloading"""
        try:
            ydl_opts = {
                'quiet': True,
                'no_warnings': True,
                'extract_flat': False,
            }
            
            with YoutubeDL(ydl_opts) as ydl:
                info = ydl.extract_info(url, download=False)
                
                # Simplify the info for Android consumption
                simplified_info = {
                    'id': info.get('id', ''),
                    'title': info.get('title', ''),
                    'uploader': info.get('uploader', ''),
                    'duration': info.get('duration', 0),
                    'view_count': info.get('view_count', 0),
                    'description': info.get('description', ''),
                    'thumbnail': info.get('thumbnail', ''),
                    'webpage_url': info.get('webpage_url', url),
                    'formats': []
                }
                
                # Extract format information
                if 'formats' in info:
                    for fmt in info['formats']:
                        format_info = {
                            'format_id': fmt.get('format_id', ''),
                            'ext': fmt.get('ext', ''),
                            'resolution': fmt.get('resolution', ''),
                            'fps': fmt.get('fps', 0),
                            'filesize': fmt.get('filesize', 0),
                            'vcodec': fmt.get('vcodec', ''),
                            'acodec': fmt.get('acodec', ''),
                        }
                        simplified_info['formats'].append(format_info)
                
                return simplified_info
                
        except Exception as e:
            self.logger.error(f"Error extracting info: {e}")
            return None
    
    def download_video(self, url: str, options: Dict[str, Any] = None) -> Dict[str, Any]:
        """Download video with specified options"""
        try:
            # Default options
            ydl_opts = {
                'outtmpl': os.path.join(self.output_dir, '%(title)s.%(ext)s'),
                'format': 'best[height<=720]',  # Default to 720p or lower
                'noplaylist': True,
                'extractaudio': False,
                'audioformat': 'mp3',
                'audioquality': '192',
            }
            
            # Update with custom options
            if options:
                ydl_opts.update(options)
            
            # Progress hook for Android
            def progress_hook(d):
                if d['status'] == 'downloading':
                    if d.get('total_bytes'):
                        percentage = (d['downloaded_bytes'] / d['total_bytes']) * 100
                        print(f"PROGRESS:{percentage:.1f}")
                elif d['status'] == 'finished':
                    print(f"COMPLETED:{d['filename']}")
                elif d['status'] == 'error':
                    print(f"ERROR:{d.get('error', 'Unknown error')}")
            
            ydl_opts['progress_hooks'] = [progress_hook]
            
            with YoutubeDL(ydl_opts) as ydl:
                info = ydl.extract_info(url, download=True)
                
                return {
                    'status': 'success',
                    'title': info.get('title', ''),
                    'filename': ydl.prepare_filename(info),
                    'id': info.get('id', '')
                }
                
        except Exception as e:
            self.logger.error(f"Error downloading video: {e}")
            return {
                'status': 'error',
                'error': str(e)
            }
    
    def get_supported_sites(self) -> List[str]:
        """Get list of supported sites"""
        try:
            from yt_dlp.extractor import list_extractor_classes
            extractors = list_extractor_classes()
            sites = []
            
            for extractor in extractors:
                if hasattr(extractor, 'IE_NAME') and extractor.IE_NAME:
                    sites.append(extractor.IE_NAME)
            
            return sorted(sites)
        except Exception as e:
            self.logger.error(f"Error getting supported sites: {e}")
            return []

def main():
    """Main function for command line usage"""
    if len(sys.argv) < 2:
        print("Usage: python ytdlp_bridge.py <command> [args...]")
        print("Commands:")
        print("  info <url> - Extract video information")
        print("  download <url> [options] - Download video")
        print("  sites - List supported sites")
        sys.exit(1)
    
    bridge = AndroidYtDlpBridge()
    command = sys.argv[1]
    
    if command == 'info':
        if len(sys.argv) < 3:
            print("Error: URL required for info command")
            sys.exit(1)
        url = sys.argv[2]
        info = bridge.extract_info(url)
        if info:
            print(json.dumps(info, indent=2))
        else:
            print("Error: Failed to extract video information")
            sys.exit(1)
    
    elif command == 'download':
        if len(sys.argv) < 3:
            print("Error: URL required for download command")
            sys.exit(1)
        url = sys.argv[2]
        
        # Parse additional options if provided
        options = {}
        if len(sys.argv) > 3:
            try:
                options = json.loads(sys.argv[3])
            except json.JSONDecodeError:
                print("Error: Invalid JSON options")
                sys.exit(1)
        
        result = bridge.download_video(url, options)
        print(json.dumps(result, indent=2))
    
    elif command == 'sites':
        sites = bridge.get_supported_sites()
        print(json.dumps(sites, indent=2))
    
    else:
        print(f"Error: Unknown command '{command}'")
        sys.exit(1)

if __name__ == '__main__':
    main()
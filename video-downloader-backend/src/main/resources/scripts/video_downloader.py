import sys
import boto3
import os
import yt_dlp
import uuid

def download_video(url, s3_bucket_name, s3_folder):
    ydl_opts = {
        'format': 'best',
        'outtmpl': '%(title)s.%(ext)s',
    }
    try:
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            info_dict = ydl.extract_info(url, download=True)
            video_file = ydl.prepare_filename(info_dict)
            print(f"Downloaded video file: {video_file}")
    except Exception as download_error:
        print(f"Failed to download YouTube video: {download_error}")
        return None

    # Upload to S3
    s3 = boto3.client('s3')
    video_filename = str(uuid.uuid1()) + ".mp4"
    s3_key = f"{s3_folder}/{video_filename}"
    try:
        s3.upload_file(video_file, s3_bucket_name, s3_key)
        print(f"Uploaded {video_file} to S3 bucket {s3_bucket_name} as {s3_key}")
    except Exception as upload_error:
        print(f"Failed to upload video to S3: {upload_error}")
        raise

    # Clean up: Delete the downloaded video file
    os.remove(video_file)

    # Return S3 URL
    s3_url = f"https://{s3_bucket_name}.s3.amazonaws.com/{s3_key}"
    return s3_url

if __name__ == "__main__":
    if len(sys.argv) != 4:
        print("Usage: python video_downloader_youtube.py <youtube_url> <s3_bucket_name> <s3_folder>")
    else:
        url = sys.argv[1]
        s3_bucket_name = sys.argv[2]
        s3_folder = sys.argv[3]
        try:
            s3_url = download_video(url, s3_bucket_name, s3_folder)
            if s3_url:
                print(f"Download completed. S3 URL: {s3_url}")
        except Exception as e:
            print(f"An error occurred: {e}")

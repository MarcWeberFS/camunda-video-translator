import sys
import instaloader
import boto3
import os
import shutil

def download_video(url, s3_bucket_name, s3_folder):
    # Get Instagram credentials from environment variables
    username = os.getenv("INSTAGRAM_USERNAME")
    password = os.getenv("INSTAGRAM_PASSWORD")

    # Initialize Instaloader with credentials
    loader = instaloader.Instaloader(
        download_comments=False,
        download_geotags=False,
        download_pictures=False,
        download_video_thumbnails=False,
        save_metadata=False
    )

    if username and password:
        try:
            loader.login(username, password)
            print("Logged in to Instagram successfully.")
        except Exception as login_error:
            print(f"Failed to login: {login_error}")
            return None
    else:
        print("Instagram credentials are not set in environment variables.")
        return None

    # Extract shortcode from the URL
    shortcode = url.split('/')[-2]
    post = instaloader.Post.from_shortcode(loader.context, shortcode)
    loader.download_post(post, target=shortcode)

    # Find the downloaded video file
    video_file = None
    for file in os.listdir(shortcode):
        if file.endswith(".mp4"):
            video_file = os.path.join(shortcode, file)
            break

    if not video_file:
        raise FileNotFoundError("Downloaded video file not found.")

    # Upload to S3
    s3 = boto3.client('s3')
    s3_key = f"{s3_folder}/{shortcode}.mp4"
    try:
        s3.upload_file(video_file, s3_bucket_name, s3_key)
        print(f"Uploaded {video_file} to S3 bucket {s3_bucket_name} as {s3_key}")
    except Exception as upload_error:
        print(f"Failed to upload video to S3: {upload_error}")
        raise

    # Clean up: Delete the downloaded video file and folder
    os.remove(video_file)
    shutil.rmtree(shortcode)  # Remove the entire folder

    # Return S3 URL
    s3_url = f"https://{s3_bucket_name}.s3.amazonaws.com/{s3_key}"
    return s3_url

if __name__ == "__main__":
    if len(sys.argv) != 4:
        print("Usage: python video_downloader_instagram.py <instagram_url> <s3_bucket_name> <s3_folder>")
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

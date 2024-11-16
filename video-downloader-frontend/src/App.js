import { useState } from 'react'
import './App.css'
import config from './config'
import Input from './components/Input'
import Toggle from './components/Toggle'
import SelectMenu from './components/SelectMenu'

function App() {
  const [url, setUrl] = useState('')
  const [advanced, setAdvanced] = useState(false)
  const [sourceLanguage, setSourceLanguage] = useState('')
  const [targetLanguage, setTargetLanguage] = useState('')
  const [message, setMessage] = useState('')
  const [downloadLink, setDownloadLink] = useState(null)

  const handleSubmit = async (event) => {
    event.preventDefault()
    setMessage('Starting download process...')
    setDownloadLink(null)

    try {
      const params = new URLSearchParams({
        url,
        advanced,
        sourceLanguage,
        targetLanguage,
      })

      console.log('params:', params)
      

      const response = await fetch(`${config.apiBaseUrl}/start-download?${params}`, {
        method: 'POST',
      })

      if (response.ok) {
        const result = await response.json()
        const instanceId = result.processInstanceId
        setMessage('Download process started. Waiting for the download link...')
        pollForDownloadLink(instanceId)
      } else {
        setMessage('Failed to start download process.')
      }
    } catch (error) {
      console.error('Error:', error)
      setMessage('An error occurred.')
    }
  }

  const pollForDownloadLink = (instanceId) => {
    const intervalId = setInterval(async () => {
      try {
        const response = await fetch(`${config.apiBaseUrl}/download-link?processInstanceId=${instanceId}`)

        if (response.ok) {
          const result = await response.json()
          setDownloadLink(result.downloadLink)
          setMessage('Download link is ready!')
          clearInterval(intervalId)
        } else if (response.status === 204) {
          setMessage('Waiting for download link to be ready...')
        } else {
          setMessage('Failed to retrieve download link.')
          clearInterval(intervalId)
        }
      } catch (error) {
        console.error('Error while polling for download link:', error)
        setMessage('An error occurred while waiting for the download link.')
        clearInterval(intervalId)
      }
    }, 5000)
  }

  return (
    <div className="container mx-auto mt-10">
      <header>
        <h1>Youtube / Instagram Video Downloader</h1>
        <form onSubmit={handleSubmit}>
          <Input value={url} onChange={(e) => setUrl(e.target.value)} />
          <br />
          <Toggle checked={advanced} onChange={setAdvanced} />
          <br />
          {advanced ? (
          <SelectMenu
          sourceLanguage={sourceLanguage}
          targetLanguage={targetLanguage}
          onChange1={setSourceLanguage}
          onChange2={setTargetLanguage}
          /> 
          ) : null}

          <br />
          <button
            type="submit"
            className="rounded-md bg-indigo-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
          >
            Download Video
          </button>
        </form>
        {message && <p>{message}</p>}
        {downloadLink && (
          <a href={downloadLink} download="video.mp4">
            Click here if the download doesn’t start automatically
          </a>
        )}
      </header>
    </div>
  )
}

export default App

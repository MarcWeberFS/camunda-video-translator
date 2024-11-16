/*
  This example requires some changes to your config:
  
  ```
  // tailwind.config.js
  module.exports = {
    // ...
    plugins: [
      // ...
      require('@tailwindcss/forms'),
    ],
  }
  ```
*/
export default function Example({value, onChange}) {
    return (
      <div className="rounded-md px-3 pb-1.5 pt-2.5 shadow-sm ring-1 ring-inset ring-gray-300 focus-within:ring-2 focus-within:ring-indigo-600">
        <label htmlFor="name" className="block text-xs font-medium text-gray-900">
          YouTube URL
        </label>
        <input
          id="url"
          name="url"
          type="text"
          value={value}
          onChange={onChange}
          placeholder="https://www.youtube.com/watch?v=dQw4w9WgXcQ"
          className="block w-full border-0 p-0 text-gray-900 placeholder:text-gray-400 sm:text-sm/6 focus:ring-0 focus:outline-none"
        />
      </div>
    )
  }
  
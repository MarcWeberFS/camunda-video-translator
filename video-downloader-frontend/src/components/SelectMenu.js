'use client'

import { useState } from 'react'
import { Listbox } from '@headlessui/react'
import { CheckIcon, ChevronUpDownIcon } from '@heroicons/react/20/solid'

const languages = [
  { value: 'en', label: 'English' },
  { value: 'de', label: 'German' },
  { value: 'fr', label: 'French' },
  { value: 'es', label: 'Spanish' },
  { value: 'it', label: 'Italian' },
  { value: 'pt', label: 'Portuguese' },
  { value: 'nl', label: 'Dutch' },
  { value: 'ru', label: 'Russian' },
  { value: 'zh', label: 'Chinese' },
  { value: 'ja', label: 'Japanese' },
  { value: 'ko', label: 'Korean' },
  { value: 'ar', label: 'Arabic' },
  { value: 'hi', label: 'Hindi' },
  { value: 'tr', label: 'Turkish' },
  { value: 'pl', label: 'Polish' },
  { value: 'sv', label: 'Swedish' },
]

function LanguageSelector({ label, selectedLanguage, onChange }) {
  const defaultSelected = languages.find((lang) => lang.value === selectedLanguage) || languages[0]

  return (
    <Listbox value={defaultSelected} onChange={onChange}>
      <Listbox.Label className="block text-sm font-medium text-gray-900">{label}</Listbox.Label>
      <div className="relative mt-2">
        <Listbox.Button className="relative w-full cursor-default rounded-md bg-white py-1.5 pl-3 pr-10 text-left text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 focus:outline-none focus:ring-2 focus:ring-indigo-600 sm:text-sm">
          <span className="block truncate">{defaultSelected.label}</span>
          <span className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2">
            <ChevronUpDownIcon aria-hidden="true" className="h-5 w-5 text-gray-400" />
          </span>
        </Listbox.Button>

        <Listbox.Options className="absolute z-10 mt-1 max-h-60 w-full overflow-auto rounded-md bg-white py-1 text-base shadow-lg ring-1 ring-black/5 focus:outline-none sm:text-sm">
          {languages.map((language) => (
            <Listbox.Option
              key={language.value}
              value={language}
              className={({ active }) =>
                `relative cursor-default select-none py-2 pl-3 pr-9 ${
                  active ? 'bg-indigo-600 text-white' : 'text-gray-900'
                }`
              }
            >
              {({ selected }) => (
                <>
                  <span className={`block truncate ${selected ? 'font-semibold' : 'font-normal'}`}>
                    {language.label}
                  </span>
                  {selected && (
                    <span className="absolute inset-y-0 right-0 flex items-center pr-4 text-indigo-600">
                      <CheckIcon aria-hidden="true" className="h-5 w-5" />
                    </span>
                  )}
                </>
              )}
            </Listbox.Option>
          ))}
        </Listbox.Options>
      </div>
    </Listbox>
  )
}

export default function SelectMenu({ sourceLanguage, targetLanguage, onChange1, onChange2 }) {
  return (
    <div className="space-y-6">
      <LanguageSelector
        label="Source Language"
        selectedLanguage={sourceLanguage}
        onChange={(language) => onChange1(language.value)}
      />
      <LanguageSelector
        label="Target Language"
        selectedLanguage={targetLanguage}
        onChange={(language) => onChange2(language.value)}
      />
    </div>
  )
}

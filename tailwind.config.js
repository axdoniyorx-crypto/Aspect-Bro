/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        cyber: {
          bg: '#0A0F1D',
          surface: '#131B2E',
          card: '#1A233A',
          border: '#233554',
          cyan: '#00E5FF',
          green: '#00E676',
          red: '#FF3D71',
          amber: '#FFB300'
        }
      }
    },
  },
  plugins: [],
}

/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Nombres descriptivos de la paleta oficial de Chronode
        'brand-blue': {
          DEFAULT: '#003E8F',
          hover: '#002D6B',
          light: '#ADD0FF',
        },
        'soft-blue': '#ADD0FF',
        'navy-dark': '#14243A',
        'sand-gold': '#FFD482',
        'golden-ochre': '#8F5E01',
        'deep-bronze': '#4F3B15',
        'pure-white': '#FFFFFF',

        // Mapeo semántico para componentes de interfaz (Shadcn UI)
        background: '#FFFFFF',
        foreground: '#14243A',
        primary: {
          DEFAULT: '#003E8F',
          hover: '#002D6B',
          foreground: '#FFFFFF',
        },
        secondary: {
          DEFAULT: '#ADD0FF',
          foreground: '#14243A',
        },
        accent: {
          DEFAULT: '#FFD482',
          foreground: '#4F3B15',
        },
        muted: {
          DEFAULT: '#F1F5F9',
          foreground: '#64748B',
        },
        border: '#E2E8F0',
        ring: '#003E8F',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
      },
      borderRadius: {
        lg: '0.625rem',
        md: '0.5rem',
        sm: '0.375rem',
      },
    },
  },
  plugins: [],
}

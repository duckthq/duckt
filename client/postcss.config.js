module.exports = {
  content: [
    "./src/**/*.{cljs,clj,cljc}",
    "./resources/public/**/*.html"
  ],
  plugins: {
    '@tailwindcss/postcss': {},
    'postcss-preset-mantine': {},
    'postcss-simple-vars': {
      variables: {
        'mantine-breakpoint-xs': '36em',
        'mantine-breakpoint-sm': '48em',
        'mantine-breakpoint-md': '62em',
        'mantine-breakpoint-lg': '75em',
        'mantine-breakpoint-xl': '88em',
      },
    },
    'autoprefixer': {},
    ...(process.env.NODE_ENV === 'production' ? { 'cssnano': { preset: 'default' } } : {}),
  },
};

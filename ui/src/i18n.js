import i18next from 'i18next';
import { initReactI18next } from 'react-i18next';

i18next.use(initReactI18next).init({
  lng: 'en',
  fallbackLng: 'en',
  resources: {
    en: {
      translation: require('./locales/en.json')
    }
  },
  interpolation: {
    escapeValue: false
  }
});

import React from 'react';
import Layout from "./Layout";
import {useTranslation} from 'react-i18next';
import PrayerRequestForm from './PrayerRequestForm';


const PrayerRequestFormPage = () => {
  const {t} = useTranslation();

  return (<Layout headerTitle={t('prayer.request')}>
    <PrayerRequestForm action='/publish'/>
  </Layout>);
}

export default PrayerRequestFormPage;

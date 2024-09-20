import React from 'react';
import Layout from './Layout';
import {useTranslation} from 'react-i18next';
import {useLoaderData} from 'react-router-dom';
import PrayerRequestForm from './PrayerRequestForm';

const EditPrayerRequestPage = () => {
  const {t} = useTranslation();
  const prayerRequest = useLoaderData();

  return (<Layout headerTitle={t('edit.prayer.request')}>
    <PrayerRequestForm action={`/prayers/${prayerRequest.id}/edit`} prayerRequest={prayerRequest}/>
  </Layout> );

}

export default EditPrayerRequestPage;

import React from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

const Footer = () => {

  const { t } = useTranslation();

  return (<footer className='footer'>
    prayerping.org: <Link className='footer-link' to='/about'>{t('about')}</Link> · <Link className='footer-link' to='/privacy'>{t('privacy.policy')}</Link><br/>
    PrayerPing: <a className='footer-link' href={process.env.REACT_APP_VIEW_CODE}>{t('view.source.code')}</a>
  </footer>);

}

export default Footer;

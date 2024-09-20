import React from 'react';
import { useTranslation } from 'react-i18next';
import { Outlet, useLoaderData } from 'react-router-dom';
import Layout from './Layout';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';

const Profile = () => {
  const { t } = useTranslation();
  const getUserResponse = useLoaderData();
  const userProfile = getUserResponse.userProfile;

  return (
    <Layout headerTitle={t('user.profile')}>
      {userProfile.avatarURL && <Row className='mt-3'>
        <Col>
          <img src={userProfile.avatarURL} alt={userProfile.name} />
        </Col>
      </Row>}
      <Row>
        <Col>
          <strong>{t('name')}</strong>: {userProfile.name ? userProfile.name : 'None'}
        </Col>
      </Row>
      <Row>
        <Col>
          <strong>{t('email')}</strong>: {userProfile.email ? userProfile.email : 'None'}
        </Col>
      </Row>
      <Outlet />
    </Layout>
  );
};

export default Profile;

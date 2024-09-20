import React from 'react';
import {useTranslation} from 'react-i18next';
import {Form as ReactRouterForm} from 'react-router-dom';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Button from 'react-bootstrap/Button';
import PasswordFieldWithStrength from './PasswordFieldWithStrength';
import Layout from './Layout';

const ResetPassword = () => {
  const { t} = useTranslation();

  return (
    <Layout headerTitle={t('reset.password')}>
      <ReactRouterForm method='post'>
        <Row className='mt-3'>
          <Col>
            <PasswordFieldWithStrength id='password' name='password' label={t('password')} />
          </Col>
        </Row>
        <Row className='mt-3'>
          <Col>
            <Button type='submit'>{t('reset')}</Button>
          </Col>
        </Row>
      </ReactRouterForm>
    </Layout>
  );
};

export default ResetPassword;

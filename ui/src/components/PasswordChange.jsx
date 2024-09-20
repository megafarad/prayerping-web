import React from 'react';
import PasswordField from './PasswordField';
import {useTranslation} from 'react-i18next';
import {Form as ReactRouterForm} from 'react-router-dom';
import PasswordFieldWithStrength from './PasswordFieldWithStrength';
import Button from 'react-bootstrap/Button';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Layout from './Layout';

const PasswordChange = () => {
  const {t} = useTranslation();

  return (
    <Layout headerTitle={t('change.password')}>
      <ReactRouterForm method='post'>
        <Row>
          <Col>
            <PasswordField
              label={t('current.password')}
              id='current-password'
              name='current-password'
            />
          </Col>
        </Row>
        <PasswordFieldWithStrength id='new-password' name='new-password' label={t('new.password')}/>
        <Row className='mt-3'>
          <Col>
            <Button type='submit'>{t('change')}</Button>
          </Col>
        </Row>
      </ReactRouterForm>
    </Layout>
  );
}

export default PasswordChange;

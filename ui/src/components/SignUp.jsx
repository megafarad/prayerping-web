import React from 'react';
import {useTranslation} from 'react-i18next';
import {Form as ReactRouterForm, useActionData} from 'react-router-dom';
import PasswordFieldWithStrength from './PasswordFieldWithStrength';
import ReCAPTCHA from 'react-google-recaptcha';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Form from 'react-bootstrap/Form';
import FloatingLabel from 'react-bootstrap/FloatingLabel';
import Button from 'react-bootstrap/Button';
import Layout from "./Layout";

const SignUp = () => {
  const { t } = useTranslation();

  const signupResponse = useActionData();
  console.log(signupResponse);

  return (
    <Layout headerTitle={t('sign.up')}>
      <ReactRouterForm method='post'>
        <Row className='mt-3'>
          <Col>
            {t('sign.up.account')}
          </Col>
        </Row>
        <Row className='mt-3'>
          <Col>
            <FloatingLabel label={t('handle')}>
              <Form.Control
                id='handle'
                name='handle'
                placeholder='Handle'
              />
            </FloatingLabel>
          </Col>
        </Row>
        <Row className='mt-3'>
          <Col>
            <FloatingLabel label={t('name')}>
              <Form.Control
                id='name'
                name='name'
                placeholder='Name'
              />
            </FloatingLabel>
          </Col>
        </Row>
        <Row className='mt-3'>
          <Col>
            <FloatingLabel label={t('email')}>
              <Form.Control
                id='email'
                name='email'
                type='email'
                placeholder='email@example.com'
              />
            </FloatingLabel>
          </Col>
        </Row>
        <PasswordFieldWithStrength id='password' name='password' label={t('password')} />
        <Row className='mt-3'>
          <Col>
            <ReCAPTCHA sitekey={process.env.REACT_APP_RECAPTCHA_SITEKEY}/>
          </Col>
        </Row>
        <Row className='mt-3'>
          <Col>
            <Button type='submit'>{t('sign.up')}</Button>
          </Col>
        </Row>
      </ReactRouterForm>

    </Layout>
  );
};

export default SignUp;

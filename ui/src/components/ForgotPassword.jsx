import React from 'react';
import {useTranslation} from 'react-i18next';
import {Form as ReactRouterForm} from 'react-router-dom';
import Form from 'react-bootstrap/Form';
import FloatingLabel from 'react-bootstrap/FloatingLabel';
import Button from 'react-bootstrap/Button';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Layout from './Layout';

const ForgotPassword = () => {
  const {t} = useTranslation();

  return (
    <Layout headerTitle={t('forgot.password')}>
      <ReactRouterForm method='post'>
        <Row>
          <Col>
            {t('forgot.password.info')}
          </Col>
        </Row>
        <Row>
          <Col>
            <FloatingLabel controlId='emailAddress' label='Email address' className='mt-3'>
              <Form.Control id='email' name='email' type='email' placeholder='name@example.com' />
            </FloatingLabel>
          </Col>
        </Row>
        <Row>
          <Col>
            <Button type='submit' variant='primary' className='mt-3'>{t('send')}</Button>
          </Col>
        </Row>
      </ReactRouterForm>
    </Layout>
  )
}

export default ForgotPassword;

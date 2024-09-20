import React from 'react';
import {useTranslation} from 'react-i18next';
import {Form as RectRouterForm, Link} from 'react-router-dom';
import Form from 'react-bootstrap/Form';
import FloatingLabel from 'react-bootstrap/FloatingLabel';
import Button from 'react-bootstrap/Button';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import SignInError from './SignInError';
import PasswordField from './PasswordField';
import Layout from './Layout';
import GoogleButton from 'react-google-button';


const SignIn = () => {
  const {t} = useTranslation();

  return (
    <Layout headerTitle={t('sign.in')}>
      <SignInError/>
      <Row className='mt-3'>
        <Col>
          {t('sign.in.credentials')}
        </Col>
      </Row>
      <RectRouterForm method='post'>
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
        <Row className='mt-3'>
          <Col>
            <PasswordField id='password' name='password' label={t('password')}/>
          </Col>
        </Row>
        <Row className='mt-3'>
          <Col>
            <Form.Check name='rememberMe' label={t('remember.me')} defaultChecked={true} value={true}/>
          </Col>
        </Row>
        <Row className='mt-3'>
          <Col>
            <Button type='submit'>{t('sign.in')}</Button>
          </Col>
        </Row>
        <Row className='mt-3'>
          <Col>
            {t('not.a.member')} <Link to='/signUp'>{t('sign.up.now')}</Link> | <Link
            to='/password/forgot'>{t('forgot.your.password')}</Link>
          </Col>
        </Row>
        <Row className='mt-3'>
          <Col>
            {t('or.use.social')}
          </Col>
        </Row>
        <Row className='mt-3'>
          <Col>
            <GoogleButton onClick={() => window.location.href='/authenticate/google'}/>
          </Col>
        </Row>
      </RectRouterForm>
    </Layout>
  )
}

export default SignIn;

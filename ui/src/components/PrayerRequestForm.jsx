import React, {useEffect, useRef} from 'react';
import { useFetcher } from 'react-router-dom';
import Form from 'react-bootstrap/Form'
import Button from 'react-bootstrap/Button';
import { useTranslation } from 'react-i18next';
import {useQueryParams} from '../hooks/useQueryParams';

const PrayerRequestForm = ({action, prayerRequest}) => {

  const {t} = useTranslation();
  const fetcher = useFetcher();
  const isSubmitting = fetcher.state === 'submitting'
  const formRef = useRef();
  const focusRef= useRef();

  const queryParams = useQueryParams();

  const redirectTo = queryParams.get('redirectTo');


  useEffect(() => {
    if (!isSubmitting) {
      formRef.current.reset();
      focusRef.current.focus();
    }
  }, [isSubmitting]);

  return (
      <div className='prayer-request-form'>
        <fetcher.Form method='post' action={action} ref={formRef}>
          <Form.Group className='mt-3 mx-3'>
            <Form.Label>{t('prayer.request')}</Form.Label>
            <Form.Control
              as='textarea'
              rows={3}
              id='request'
              name='request'
              defaultValue={prayerRequest?.request}
              placeholder={t('enter.prayer.request')}
              ref={focusRef}
            />
          </Form.Group>
          <Form.Group className='my-3 mx-3'>
            <Form.Check
              type='checkbox'
              label={t('make.prayer.request.anonymous')}
              id='isAnonymous'
              name='isAnonymous'
              value='true'
              defaultChecked={prayerRequest?.isAnonymous}
            />
          </Form.Group>
          {redirectTo && <input type="hidden" name='redirectTo' value={redirectTo} />}
          <Button variant='primary' type='submit' className='mx-3' disabled={isSubmitting}>
            {t('submit')}
          </Button>
        </fetcher.Form>
      </div>
    );
}

export default PrayerRequestForm;

import React, {useEffect, useRef} from 'react';
import { useFetcher } from 'react-router-dom';
import Form from 'react-bootstrap/Form';
import Button from 'react-bootstrap/Button';
import { useTranslation } from 'react-i18next';

const PrayerResponseForm = ({prayerId}) => {
  const {t} = useTranslation();
  const fetcher = useFetcher();
  const isSubmitting = fetcher.state === 'submitting'
  const formRef = useRef();
  const focusRef= useRef();

  useEffect(() => {
    if (!isSubmitting) {
      formRef.current.reset();
      focusRef.current.focus();
    }
  }, [isSubmitting]);

  return (
    <div className='prayer-response-form'>
      <fetcher.Form method='POST' action={`/prayers/${prayerId}/respond`} ref={formRef}>
        <Form.Group className='mt-3 mx-3'>
          <Form.Label>{t('response')}</Form.Label>
          <Form.Control
            as='textarea'
            rows = {3}
            id='response'
            name='response'
            placeholder={t('enter.response')}
            ref={focusRef}
          />
        </Form.Group>
        <Button variant='primary' type='submit' className='mx-3 my-3' disabled={isSubmitting}>
          {t('respond')}
        </Button>
      </fetcher.Form>
    </div>
  );
};

export default PrayerResponseForm;

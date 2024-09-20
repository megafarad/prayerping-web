import React, { useEffect, useState } from 'react';
import { clearApiResponse } from '../redux/apiResponseSlice';
import { useSelector, useDispatch } from 'react-redux';
import { useTranslation } from 'react-i18next';
import Toast from 'react-bootstrap/Toast';
import ToastContainer from 'react-bootstrap/ToastContainer';

const ApiResponse = () => {
  const apiResponse = useSelector((state) => state.apiResponse.response);
  const dispatch = useDispatch();
  const { t } = useTranslation();
  const [show, setShow] = useState(false);

  useEffect(() => {
    if (apiResponse) {
      setShow(true);
      setTimeout(() => {
        setShow(false);
        dispatch(clearApiResponse());
      }, 10000); // Auto hide after 10 seconds
    }
  }, [apiResponse, dispatch]);

  const onClose = () => {
    setShow(false);
    dispatch(clearApiResponse());
  };

  return (
    <ToastContainer position="bottom-end" className="p-3">
      {apiResponse?.info && (
        <Toast show={show} onClose={onClose} delay={10000} autohide bg="info" className='toast-info'>
          <Toast.Header>
            <strong className="me-auto">{t('info')}</strong>
          </Toast.Header>
          <Toast.Body className='text-white'>{apiResponse.info}</Toast.Body>
        </Toast>
      )}
      {apiResponse?.success && (
        <Toast show={show} onClose={onClose} delay={10000} autohide bg="success" className='toast-success'>
          <Toast.Header>
            <strong className="me-auto">{t('success')}</strong>
          </Toast.Header>
          <Toast.Body className='text-white'>
            {apiResponse.success}
          </Toast.Body>
        </Toast>
      )}
      {apiResponse?.error && (
        <Toast show={show} onClose={onClose} delay={10000} autohide bg="danger" className='toast-danger'>
          <Toast.Header>
            <strong className="me-auto">{t('error')}</strong>
          </Toast.Header>
          <Toast.Body className='text-white'>
            {apiResponse.error}
          </Toast.Body>
        </Toast>
      )}
    </ToastContainer>
  );
};

export default ApiResponse;

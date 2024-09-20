import React from 'react';
import Modal from 'react-bootstrap/Modal';
import Button from 'react-bootstrap/Button';
import {useTranslation} from 'react-i18next';

const DeleteModal = ({showModal, bodyMessage, handleCloseModal, handleDelete}) => {

  const {t} = useTranslation();

  return (<Modal show={showModal} onHide={handleCloseModal}>
    <Modal.Header closeButton>
      <Modal.Title>{t('delete.modal.title')}</Modal.Title>
    </Modal.Header>
    <Modal.Body>{bodyMessage}</Modal.Body>
    <Modal.Footer>
      <Button variant="secondary" onClick={handleCloseModal}>
        {t('cancel')}
      </Button>
      <Button variant="danger" onClick={handleDelete}>
        {t('delete')}
      </Button>
    </Modal.Footer>
  </Modal>)
};

export default DeleteModal;

import React, {useEffect, useRef, useCallback, useMemo} from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchPrayerResponses, clearResponses } from '../redux/prayerResponseSlice';
import { useLoaderData } from 'react-router-dom';
import { createSelector } from '@reduxjs/toolkit';
import { useTranslation } from 'react-i18next';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import PrayerRequestCard from './PrayerRequestCard';
import Layout from './Layout';
import PrayerResponseCard from './PrayerResponseCard';
import PrayerResponseForm from './PrayerResponseForm';
import { useQueryParams } from '../hooks/useQueryParams';
import Spinner from "react-bootstrap/Spinner";

const PrayerRequestPage = () => {

  const prayer = useLoaderData();
  const dispatch = useDispatch();
  const user = useSelector((state) => state.user.userProfile);

  const webSocketStatus = useSelector((state) => state.webSocketState.status);

  const selectPrayerResponses = state => state.prayerResponses.responses;
  const selectPrayerId = (state, prayerId) => prayerId;

  const makeSelectResponses = () => createSelector(
    [selectPrayerResponses, selectPrayerId],
    (responses, prayerId) => responses[prayerId] || []
  );

  const selectResponses = useMemo(makeSelectResponses, []);
  const responses = useSelector(state => selectResponses(state, prayer.id));
  const loading = useSelector(state => state.prayerResponses.loading[prayer.id]);
  const hasMore = useSelector(state => state.prayerResponses.hasMore[prayer.id]);
  const page = useRef(0);

  const queryParams = useQueryParams();
  const showResponseForm = queryParams.get('showResponseForm') === 'true';

  useEffect(() => {
    // Fetch the initial responses
    dispatch(fetchPrayerResponses({ prayerId: prayer.id, page: page.current }));

    return () => {
      // Clear responses when the component unmounts
      dispatch(clearResponses({ prayerId: prayer.id }));
    };
  }, [dispatch, prayer.id]);

  useEffect( () => {
    if (webSocketStatus === 'connected') {
      dispatch({type: 'webSocket/send', payload: {type: 'subscribe', channel: `prayer.${prayer.id}.response`}});
    }
    return () => {
      if (webSocketStatus === 'connected') {
        dispatch({type: 'webSocket/send', payload: {type: 'unsubscribe', channel: `prayer.${prayer.id}.response`}});
      }
    }
  }, [dispatch, prayer.id, webSocketStatus]);

  const observer = useRef();
  const lastResponseElementRef = useCallback(node => {
    if (loading) return;
    if (observer.current) observer.current.disconnect();
    observer.current = new IntersectionObserver(entries => {
      if (entries[0].isIntersecting && hasMore) {
        page.current += 1;
        dispatch(fetchPrayerResponses({ prayerId: prayer.id, page: page.current }));
      }
    });
    if (node) observer.current.observe(node);
  }, [loading, hasMore, dispatch, prayer.id]);

  const {t} = useTranslation();

  return (
    <Layout headerTitle={t('prayer.request')}>
      <Row>
        <Col xs={12} sm={12} md={12} lg={12}>
          <PrayerRequestCard prayer={prayer}/>
        </Col>
      </Row>
      {user && showResponseForm && <Row>
        <Col xs={12} sm={12} md={12} lg={12}>
          <PrayerResponseForm prayerId={prayer.id}/>
        </Col>
      </Row> }
      <div className='prayer-request-subheader'>
        <h1>{t('responses')}</h1>
      </div>
      {responses.map((response, index) => {
        if (index === responses.length - 1) {
          return (
            <Row key={index} ref={lastResponseElementRef}>
              <Col xs={12} sm={12} md={12} lg={12}>
                <PrayerResponseCard response={response}/>
              </Col>
            </Row>
          );
        } else {
          return (
            <Row key={index}>
              <Col xs={12} sm={12} md={12} lg={12}>
                <PrayerResponseCard response={response}/>
              </Col>
            </Row>
          );
        }
      })}
      { loading && (
        <div className='text-center my-4'>
          <Spinner animation='border'/>
        </div>
      )}
      {responses.length === 0 && !loading && (
        <div className='text-center my-4'>
          {t('no.responses')}
        </div>
      )}
    </Layout>);


};

export default PrayerRequestPage;

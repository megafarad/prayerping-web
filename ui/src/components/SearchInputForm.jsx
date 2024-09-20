import React from 'react';
import { Form as ReactRouterForm } from 'react-router-dom';
import SearchInput from './SearchInput';

const SearchInputForm = () => {

  return (
    <div className='search-form'>
      <ReactRouterForm method='post' action='/search'>
        <SearchInput/>
      </ReactRouterForm>
    </div>
  );
}

export default SearchInputForm;

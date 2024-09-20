import React from 'react';
import Form from 'react-bootstrap/Form';
import InputGroup from 'react-bootstrap/InputGroup';
import { FaSearch } from 'react-icons/fa';

const SearchInput = () => {
  return (
    <InputGroup>
      <InputGroup.Text id='search-icon'>
        <FaSearch />
      </InputGroup.Text>
      <Form.Control
        type='text'
        placeholder='Search...'
        id='search'
        name='search'
        aria-label='Search'
        aria-describedby='search-icon'
      />
    </InputGroup>
  );
};

export default SearchInput;

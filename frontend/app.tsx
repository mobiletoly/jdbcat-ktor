import * as React from 'react';
import * as ReactDOM from 'react-dom';
import axios, { AxiosRequestConfig, AxiosPromise } from 'axios';
import Button from 'react-bootstrap/lib/Button';
import Table from 'react-bootstrap/lib/Table';

interface Department {
    code: String,
    name: String,
    city: String,
    countryCode: String
}

interface Employee {
    id: String,
    firstName: String,
    lastName: String,
    age: Number,
    departmentCode: String,
    dateCreated: Date
}

class App extends React.Component<
    {greeting: string},
    {departments: Department[]}
> {
  state = {
      departments: Array<Department>()
  };
  render() {
      return (
          <div>
              <Button onClick={
                  () => {
                      axios
                          .get('/api/v1/departments')
                          .then((response) => {
                              this.setState(
                                  {departments: response.data}
                              );
                          });
                  }
              }>Get all Departments
              </Button>

              <Table striped bordered hover>
                  <thead>
                  <tr>
                      <th>Code</th>
                      <th>Name</th>
                      <th>City, Country</th>
                  </tr>
                  </thead>
                  <tbody>
                  {
                      this.state.departments.map((department) =>
                          <tr>
                              <td>{department.code}</td>
                              <td>{department.name}</td>
                              <td>{department.city}, {department.countryCode}</td>
                          </tr>
                      )
                  }
                  </tbody>
              </Table>
          </div>
      );
  }
}
ReactDOM.render(
  <App greeting="Hello, world!"/>,
  document.getElementById('app')
);

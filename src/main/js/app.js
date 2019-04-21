const React = require('react');
const ReactDOM = require('react-dom');

class App extends React.Component {

    constructor(props) {
        super(props);
        // this.state = {employees: []};
    }

    componentDidMount() {
    }

    render() {
        return (
            "<p>hello!!!</p>"
        )
    }
}

ReactDOM.render(
    <App/>,
    document.getElementById('react')
)
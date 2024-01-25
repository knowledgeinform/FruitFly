module.exports = {
  echo: function (request, response) {
    if (request.method === 'GET') {
      console.log('In GET');
      let body = 'hello';
      response.end(body);
    } else if (request.method === 'POST') {
      console.log('In POST');
      let body = [];
      request.on('data', (chunk) => {
        body.push(chunk);
      }).on('end', () => {
        body = Buffer.concat(body).toString();
        response.end(body);
      });
    } else {
      console.log('404');
      response.statusCode = 404;
      response.end();
    }
  }
};

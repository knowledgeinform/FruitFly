var assert = require('assert');
const message = require('../backend/message.js');
const dataManager = require('../data.js');

describe('data.js', function() {
  var rawLocData = "LAT: 3927.6990N, LON: 07610.3086W, ALT: 10.1, TIME: 17:51:33.990000";
  var rawConcData = "Conc: 5.481, PA1: 8.6907674, PA2: 5.5963846-6.0927060j, X:1.10-0.49j,  Y:1.16-0.57j,  Z:1.14-0.45j, L1: -119.14+97.77j, L2: -98.01+83.00j";
  var rawMessageData = "Error!! Something happened";
  describe('ParseData', function() {
    it('should emit \'received\'', function(done) {
      this.timeout(1000); //timeout with an error if done() isn't called within one second

      message.messageEmitter.once('received', (m) => {
        // event emitted
        assert(true);
        done();
      });
      dataManager.ParseData(rawLocData);
    });
    it('message should contain certain fields', function(done) {
      this.timeout(1000); //timeout with an error if done() isn't called within one second

      message.messageEmitter.once('received', (m) => {
        // field assertions
        // console.log(m[0].keys());
        // assert(true);
        assert(m.hasOwnProperty("ID"));
        assert(m.hasOwnProperty("Number"));
        assert(m.hasOwnProperty("DataType"));
        assert(m.hasOwnProperty("Data"));
        done();
      });
      dataManager.ParseData(rawLocData);
    });

    it('message fields should have default values', function(done) {
      this.timeout(1000); //timeout with an error if done() isn't called within one second

      message.messageEmitter.once('received', (m) => {
        // field assertions
        // console.log(m[0].keys());
        // assert(true);
        assert.equal(m.ID,"agent1");
        assert.equal(m.Number,0);
        assert.equal(m.DataType,"Location");
      });
      dataManager.ParseData(rawLocData);

      message.messageEmitter.once('received', (m) => {
        // field assertions
        // console.log(m[0].keys());
        // assert(true);
        assert.equal(m.ID,"agent1");
        assert.equal(m.Number,0);
        assert.equal(m.DataType,"Concentration");
      });
      dataManager.ParseData(rawConcData);

      message.messageEmitter.once('received', (m) => {
        // field assertions
        // console.log(m[0].keys());
        // assert(true);
        assert.equal(m.ID,"agent1");
        assert.equal(m.Number,0);
        assert.equal(m.DataType,"UNKNOWN");
        done();
      });
      dataManager.ParseData(rawMessageData);
    });

    it('Location Data should contain certain fields', function(done) {
      this.timeout(1000); //timeout with an error if done() isn't called within one second

      message.messageEmitter.once('received', (m) => {
        // field assertion
        assert(m.Data.hasOwnProperty("Time"));
        assert(m.Data.hasOwnProperty("Lat"));
        assert(m.Data.hasOwnProperty("Lng"));
        assert(m.Data.hasOwnProperty("Alt"));
        assert(m.Data.hasOwnProperty("HMSTime"));
        done();
      });
      dataManager.ParseData(rawLocData);
    });

    it('Location Data should be parsed and calculated properly', function(done) {
      this.timeout(1000); //timeout with an error if done() isn't called within one second

      message.messageEmitter.once('received', (m) => {
        // field assertion
        assert.equal(m.Data.Lat,39.46165);
        assert.equal(m.Data.Lng,-76.17181);
        assert.equal(m.Data.Alt,10.1);
        assert.equal(m.Data.HMSTime.HH,17);
        assert.equal(m.Data.HMSTime.MM,51);
        assert.equal(m.Data.HMSTime.SS,33);
        done();
      });
      dataManager.ParseData(rawLocData);
    });

    it('Concentration Data should contain certain fields', function(done) {
      this.timeout(1000); //timeout with an error if done() isn't called within one second

      message.messageEmitter.once('received', (m) => {
        // field assertion
        assert.equal(m.DataType,"Concentration");
        assert(m.Data.hasOwnProperty("Time"));
        assert(m.Data.hasOwnProperty("Dat"));

        message.messageEmitter.once('received', (m) => {
          // field assertion
          assert.equal(m.DataType,"PAC1");
          assert(m.Data.hasOwnProperty("Time"));
          assert(m.Data.hasOwnProperty("Dat"));

          message.messageEmitter.once('received', (m) => {
            // field assertion
            assert.equal(m.DataType,"PAC2");
            assert(m.Data.hasOwnProperty("Time"));
            assert(m.Data.hasOwnProperty("Dat"));

            message.messageEmitter.once('received', (m) => {
              // field assertion
              assert.equal(m.DataType,"x");
              assert(m.Data.hasOwnProperty("Time"));
              assert(m.Data.hasOwnProperty("Dat"));

              message.messageEmitter.once('received', (m) => {
                // field assertion
                assert.equal(m.DataType,"y");
                assert(m.Data.hasOwnProperty("Time"));
                assert(m.Data.hasOwnProperty("Dat"));

                message.messageEmitter.once('received', (m) => {
                  // field assertion
                  assert.equal(m.DataType,"z");
                  assert(m.Data.hasOwnProperty("Time"));
                  assert(m.Data.hasOwnProperty("Dat"));

                  message.messageEmitter.once('received', (m) => {
                    // field assertion
                    assert.equal(m.DataType,"L1");
                    assert(m.Data.hasOwnProperty("Time"));
                    assert(m.Data.hasOwnProperty("Dat"));

                    message.messageEmitter.once('received', (m) => {
                      // field assertion
                      assert.equal(m.DataType,"L2");
                      assert(m.Data.hasOwnProperty("Time"));
                      assert(m.Data.hasOwnProperty("Dat"));

                      done();
                    });
                  });
                });
              });
            });
          });
        });

      });
      dataManager.ParseData(rawConcData);
    });

    it('Concentration Data should be calculated properly', function(done) {
      this.timeout(1000); //timeout with an error if done() isn't called within one second

      message.messageEmitter.once('received', (m) => {
        // field assertion
        // concentration
        assert.equal(m.Data.Dat, 5.481);

        message.messageEmitter.once('received', (m) => {
          // field assertion
          // assert.equal(m.DataType,"PAC1");
          assert.equal(m.Data.Dat, 8.6907674);

          message.messageEmitter.once('received', (m) => {
            // field assertion
            // assert.equal(m.DataType,"PAC2");
            assert.equal(m.Data.Dat,8.272882629020744);

            message.messageEmitter.once('received', (m) => {
              // field assertion
              // assert.equal(m.DataType,"x");
              assert.equal(m.Data.Dat,1.2042009799032718);

              message.messageEmitter.once('received', (m) => {
                // field assertion
                //assert.equal(m.DataType,"y");
                assert.equal(m.Data.Dat,1.2924782396620842);

                message.messageEmitter.once('received', (m) => {
                  // field assertion
                  //assert.equal(m.DataType,"z");
                  assert.equal(m.Data.Dat,1.2256018929489298);

                  message.messageEmitter.once('received', (m) => {
                    // field assertion
                    //assert.equal(m.DataType,"L1");
                    assert.equal(m.Data.Dat,154.12109686866364);

                    message.messageEmitter.once('received', (m) => {
                      // field assertion
                      //assert.equal(m.DataType,"L2");
                      assert.equal(m.Data.Dat,128.43270650422346);

                      done();
                    });
                  });
                });
              });
            });
          });
        });

      });
      dataManager.ParseData(rawConcData);
    });
  });
});

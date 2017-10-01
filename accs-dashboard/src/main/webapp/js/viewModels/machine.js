/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

define(['ojs/ojcore', 'knockout', 'ojs/ojknockout'],
        function (oj, ko) {





            function MachineViewModel($params) {
                var self = this;
                self.machineId = ko.observable("");

                self.orientationValue = ko.observable('vertical');

                var lineSeries = [{name: self.machineId(), items: []}];
                var lineGroups = [];
                for (var i = 1; i <= 120; i++) {
                    //lineGroups.push(i.toString());
                    if ( i%24 == 0 ) {
                    lineGroups.push(i/6);
                } else {
                    lineGroups.push("");
                }
                }

                //var lineGroups = ["10s", "20s", "30s", "40s", "50s", "60s", "70s", "80s", "90s", "100s"];


                self.lineSeriesValue = ko.observableArray(lineSeries);
                self.lineGroupsValue = ko.observableArray(lineGroups);
                self.ready = ko.observable(false);

//                To be removed
//                self.loadData = function (data) {
//                    console.log("insideLoadData");
//                    console.log(data);
//                };

                //console.log("In the constructor of MachineModel");

                //self.machineId = ko.observable("");
                self.unitmachineId = $params;
                //console.log("here is my initialization" + self.unitmachineId);
                self.machineId(self.unitmachineId);
                self.allMachineMap = new Map();
                self.allMachineData = ko.observable(self.allMachineMap);

                self.source = new EventSource('/sse/machine');
                self.source.onopen = function (event) {
                    console.log("Opened Machine");
                    //console.log(event);
                };

                self.source.onerror = function (event) {
                    console.log("Error in  Machine.js");
                    console.log(event);
                };

                console.log(self.source);

                self.source.onmessage = function (event) {
                    //console.log("Hurrah from Machine");
                    //console.log(event);
                    var machineData = JSON.parse(event.data);
                    //console.log(machineData);
                    var key = machineData['machine'];
                    var receivedValues = machineData['metrics']
                    var arrayValues = [];
                    if (receivedValues.length < 110) {
                        var remElements = 110 - receivedValues.length;
                        var zeroArray = Array(remElements).fill('0');
                        arrayValues = zeroArray.concat(receivedValues);
                    } else {
                        arrayValues = receivedValues;
                    }

                    arrayValues = arrayValues.slice(0, 110).map(parseFloat);
                    //console.log("machine added with key =" + key + " and metrics = " + arrayValues.toString());
                    self.allMachineMap.set(key, arrayValues);
                    if (key === self.machineId()) {
                        console.log("Selected Machine id = " + key);
                        //console.log(arrayValues);
                        self.lineSeriesValue()[0]['items'] = arrayValues;
                        self.lineSeriesValue.valueHasMutated();

                    }

                };



                self.handleDeactivated = function (info) {
                    console.log("in handleDeactivated");
                    self.source.close();
                    self.resetMachineGraph();
                };

                self.resetMachineGraph = function () {
                    self.lineSeriesValue()[0]['items'] = [];
                    self.lineSeriesValue.valueHasMutated();
                };

                self.handleActivated = function (info) {
                    //console.log("called handleactivated in machine");
                    self.parentContext = ko.contextFor(info.element.parentElement);
                    //console.log("Selected machine Id = " + self.parentContext.$data.machineId());

                    // Retrieve the childRouter instance created in main.js
                    //self.machineId = self.parentContext.$data.machineId;
                    console.log("I am in machine.js ====" + self.machineId());



                    // Returns the sync promise to handleActivated. The next
                    // phase of the ojModule lifecycle (attached) will not be
                    // executed until sync is resolved.
                    return oj.Router.sync();
                };

                self.goHome = function (data, event) {
                    self.machineId("");
                    oj.Router.rootInstance.go("root");

                    return true;
                };

            }
            ;
            return  MachineViewModel;
        });
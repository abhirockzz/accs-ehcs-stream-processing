define(['ojs/ojcore', 'knockout', 'ojs/ojknockout'],
        function (oj, ko) {
            function GaugeViewModel() {

                var self = this;
                self.value1 = ko.observable(20);
                self.value2 = ko.observable(80);
                //green - #68c182
                //red - "#ed6647" 
                //yellow - "#fad55c"
                //this.thresholdValues = [{max: 33, color : "#ed6647" }, {max: 67 , color : "blue" }, {}];
                this.thresholdValues = [{max: 33, color: "#68c182"}, {max: 75, color: "#fad55c"}, {color: "#ed6647"}];
                self.statusmeterGaugeData = function () {

                };

                self.cpuArray = ko.observableArray([]);
                self.cpuDatasource = new oj.ArrayTableDataSource(self.cpuArray, {idAttribute: 'name'});

                self.changeSelection = function (event, ui) {
                    //console.log(ui);
                    if (ui.option === 'selection') {
                        //console.log(ui.value[0]);
                        if (ui.value[0]) {
                            var machineId = ui.value[0].startKey.row;
                            //console.log(machineId);
                            self.loadMachinePage(machineId);
                        }
                    }
                };


                self.handleActivated = function (info) {
                    //console.log("called handleactivated in gauge");
                    self.parentContext = ko.contextFor(info.element.parentElement);
                    console.log("Selected machine Id = " + self.parentContext.$data.machineId());



                };



                self.loadMachinePage = function (machine) {
                    console.log("Navigating to " + machine);
                    //history.pushState(null, '', 'index.html?root=machine&emp=' + machine);
                    //oj.Router.sync();
                    self.parentContext.$data.machineId(machine);
                    self.parentContext.$data.router.go(machine);
                };



                //self.source = new EventSource('http://192.168.99.100:8080/dashboard/test/sse');
                self.source = new EventSource('/sse/leaderboard');
                self.source.onmessage = function (event) {
                    console.log("Leaderboard Stream data :");
                    //console.log(event);
                    var leaderBoard = JSON.parse(event.data);
                    //console.log(leaderBoard);
                    var listValues = leaderBoard['leaderboard'];
                    console.log(listValues);
                    self.cpuArray(listValues);

                    self.value1(21.0);
                };

                self.source.onopen = function (event) {
                    console.log("Opened leaderboard stream");
                    console.log(event);
                };

                self.source.onerror = function (event) {
                    console.log("Error");
                    console.log(event);
                };
            }
            ;

            return new GaugeViewModel();
        }
);

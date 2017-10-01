/**
 * Copyright (c) 2014, 2017, Oracle and/or its affiliates.
 * The Universal Permissive License (UPL), Version 1.0
 */

define(['ojs/ojcore', 'knockout', 'ojs/ojknockout', 'ojs/ojrouter'],
        function (oj, ko) {
            var router = oj.Router.rootInstance;
            oj.Router.defaults['urlAdapter'] = new oj.Router.urlParamAdapter();
//            router.configure(
//                        {   'machine': {label: 'Machine',
//                                enter: function () {
//                                    var childRouter = router.createChildRouter('emp');
//                                    childRouter.defaultStateId = '';
//                                    router.currentState().value = childRouter;
//                                },
//                                exit: function () {
//                                    var childRouter = router.currentState().value;
//                                    childRouter.dispose();
//                                }
//                            }
//                        });

            router.configure(function (stateId)
            {
                var state;

                if (stateId)
                {
                    var data = stateId;
                    if (data)
                    {
                        state = new oj.RouterState(stateId, {value: data});
                    }
                }
                return state;
            });

            function ControllerViewModel() {
                var self = this;
                self.router = router;
                // Media queries for repsonsive layouts
                var smQuery = oj.ResponsiveUtils.getFrameworkQuery(oj.ResponsiveUtils.FRAMEWORK_QUERY_KEY.SM_ONLY);
                self.smScreen = oj.ResponsiveKnockoutUtils.createMediaQueryObservable(smQuery);
                // Header
                // Application Name used in Branding Area
                self.appName = ko.observable("Datacenter Monitoring Application");
                // User Info used in Global Navigation area
                self.userLogin = ko.observable("john.doe");

                //selected machine id
                self.machineId = ko.observable("");
                
                self.isMachineSelected = ko.computed (function() {
                    if (self.machineId() === "") {
                        return false;
                    }
                    return true;
                });
                // Footer
                function footerLink(name, id, linkTarget) {
                    this.name = name;
                    this.linkId = id;
                    this.linkTarget = linkTarget;
                }
                self.footerLinks = ko.observableArray([
                    new footerLink('About Oracle', 'aboutOracle', 'http://www.oracle.com/us/corporate/index.html#menu-about'),
                    new footerLink('Contact Us', 'contactUs', 'http://www.oracle.com/us/corporate/contact/index.html'),
                    new footerLink('Legal Notices', 'legalNotices', 'http://www.oracle.com/us/legal/index.html'),
                    new footerLink('Terms Of Use', 'termsOfUse', 'http://www.oracle.com/us/legal/terms/index.html'),
                    new footerLink('Your Privacy Rights', 'yourPrivacyRights', 'http://www.oracle.com/us/legal/privacy/index.html')
                ]);
            }

            return new ControllerViewModel();
        }
);

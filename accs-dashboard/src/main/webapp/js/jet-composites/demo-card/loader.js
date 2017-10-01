define(['ojs/ojcore', 'text!./demo-card.html', './demo-card', 'text!./component.json', 'css!./demo-card.css', 'ojs/ojcomposite'],
  function(oj, view, viewModel, metadata) {
    oj.Composite.register('demo-card', {
      view: {inline: view}, 
      viewModel: {inline: viewModel}, 
      metadata: {inline: JSON.parse(metadata)}
    });
  }
);
const webpack = require('webpack');
const HtmlWebpackPlugin = require('html-webpack-plugin')
const CopyPlugin = require('copy-webpack-plugin');
const path = require('path');

module.exports = env => ({
  target: 'web',
  entry: {
    index: './src/assets/js/index.js',
    setup: './src/assets/js/setup.js',
    sync: './src/assets/js/sync.js',
  },
  output: {
    path: path.join(__dirname, 'dist'),
    filename: './js/[name].bundle.js',
    library: '[name]'
  },
  devServer: {
    contentBase: path.join(__dirname, 'dist'),
    compress: true,
    port: 9000,
    watchContentBase: true
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        use: 'babel-loader',
        exclude: /node_modules/
      }
    ]
  },
  plugins: [
    new webpack.DefinePlugin({
      dataSyncVersion: JSON.stringify(require('./package.json').version),
      dataSyncMode: JSON.stringify(env && env.NODE_ENV || 'development')
    }),
    new CopyPlugin([
      { from: './src/assets', to: './assets' },
      { from: './node_modules/@mdi/font/fonts', to: './assets/mdi/fonts' },
      { from: './node_modules/@mdi/font/css', to: './assets/mdi/css' },
    ]),
    new HtmlWebpackPlugin({
      template: './src/index.html',
      filename: 'index.html',
      chunks: ['index']
    }),
    new HtmlWebpackPlugin({
      template: './src/sync.html',
      filename: 'sync.html',
      chunks: ['index', 'sync']
    }),
    new HtmlWebpackPlugin({
      template: './src/setup.html',
      filename: 'setup.html',
      chunks: ['index', 'setup']
    }),
    new HtmlWebpackPlugin({
      template: './src/log.html',
      filename: 'log.html',
      chunks: []
    }),
    new HtmlWebpackPlugin({
      template: './src/404.html',
      filename: '404.html',
      chunks: []
    })
  ]
});
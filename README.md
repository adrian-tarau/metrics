# Metrics

A simple wrapper over a third party library (currently Micrometer). It exposes a _facade_ to get access to counter, gauge, histogram and timer, which are the most used meters.

It also allows defining meter groups, which makes sure all meters have the same namespace.

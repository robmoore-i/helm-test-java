# Example

Contains the Helm chart for a fictional "Gym register" application, whose template rendering is tested using `helm-test-java`.

I've never actually installed this made-up chart, although that is beside the point. What is proven by these fast running tests is that the chart renders as expected in all foreseen configurations. This is a precondition to knowing that it will behave as you expect in a real Kubernetes cluster. To verify that the resulting Kubernetes objects behave correctly in a real cluster, you need to write a higher level test using a real Kubernetes server, for example [k3d](https://k3d.io/stable).

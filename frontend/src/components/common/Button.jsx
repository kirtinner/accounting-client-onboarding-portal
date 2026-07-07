export default function Button({ children, variant = 'secondary', className = '', ...props }) {
  return (
    <button className={`button ${variant}-button ${className}`} type="button" {...props}>
      {children}
    </button>
  );
}

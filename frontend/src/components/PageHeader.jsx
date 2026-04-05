function PageHeader({ eyebrow, title, subtitle, action }) {
  return (
    <header className="page-header panel">
      <div>
        <p className="eyebrow">{eyebrow}</p>
        <h2>{title}</h2>
        {subtitle ? <p className="subtitle">{subtitle}</p> : null}
      </div>
      {action ? <div className="header-action">{action}</div> : null}
    </header>
  );
}

export default PageHeader;
